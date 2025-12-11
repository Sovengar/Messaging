package jon.messaging.orders.application.commands;

import jon.messaging.orders.domain.Order;
import jon.messaging.orders.domain.OrderSpringJpaRepo;
import jon.messaging.shared.messaging.CommandBus;
import jon.messaging.shared.messaging.RabbitPublisher;
import jon.messaging.shared.messaging.commands.PlaceOrderCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
class PlaceOrderHttpController {
    private final CommandBus commandBus;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody PlaceOrderRequest request) {
        commandBus.send(new PlaceOrderCommand(request.orderId(), request.productId()));
        return ResponseEntity.ok(request.orderId().toString());
    }

    record PlaceOrderRequest(String productId, UUID orderId) { }
}

@Component
@RequiredArgsConstructor
@Slf4j
public class PlaceOrder {
    private final OrderSpringJpaRepo repo;
    private final ApplicationEventPublisher events;
    //private final RabbitPublisher rabbitPublisher;

    @Transactional
    public void handle(PlaceOrderCommand command) {
        log.info("BEGIN PlaceOrder");

        var orderId = java.util.UUID.randomUUID();
        Order order = Order.Factory.create(Order.Id.of(orderId), command.productId());
        repo.save(order);

        publishEvent(order);
        log.info("END PlaceOrder");
    }

    /**
     * TODO
     *   Para probar el retry:
     *     Lanza una excepción dentro de tu listener.
     *     Verifica que el evento queda pendiente.
     *     Corrige el error.
     *     Verás que el evento se reintenta automáticamente (con @EnableScheduling activo).
     */
    private void publishEvent(final Order order) {
        var event = order.place();
        events.publishEvent(event);
       // rabbitPublisher.publish(event);
    }

}

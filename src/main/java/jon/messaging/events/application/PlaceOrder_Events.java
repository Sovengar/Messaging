package jon.messaging.events.application;

import jakarta.annotation.Nullable;
import jon.messaging._shared.Order;
import jon.messaging._shared.OrderRepo;
import jon.messaging.bus.Command;
import jon.messaging.events.infra.rabbitmq.RabbitPublisher;
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
@RequestMapping("/events/orders")
@RequiredArgsConstructor
class PlaceOrderEventsController {
    private final PlaceOrder_Events handler;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody PlaceOrderRequest request) {
        handler.handle(new PlaceOrder_Events.PlaceOrderCommand(request.orderId(), request.productId()));
        return ResponseEntity.ok(request.orderId().toString());
    }

    record PlaceOrderRequest(String productId, UUID orderId) { }
}

@Component
@RequiredArgsConstructor
@Slf4j
class PlaceOrder_Events {
    private final OrderRepo repo;
    private final ApplicationEventPublisher events;
    private final RabbitPublisher rabbitPublisher;

    @Transactional
    public UUID handle(PlaceOrderCommand command) {
        log.info("BEGIN PlaceOrder");

        var orderId = command.orderId() != null ? command.orderId() : UUID.randomUUID();
        Order order = Order.Factory.create(Order.Id.of(orderId), command.productId());
        repo.save(order);

        publishEvent(order);
        log.info("END PlaceOrder");

        return orderId;
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
        rabbitPublisher.publish(event);
    }

    public record PlaceOrderCommand(@Nullable UUID orderId, String productId) implements Command {
    }
}

package jon.messaging.orders.application.commands;

import jon.messaging.old.commands.PlaceOrderCommand2;
import jon.messaging.orders.domain.Order;
import jon.messaging.orders.domain.OrderRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlaceOrder2 {
    private final OrderRepo repo;
    private final ApplicationEventPublisher events;
    //private final RabbitPublisher rabbitPublisher;

    @Transactional
    public UUID handle(PlaceOrderCommand2 command) {
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
        // rabbitPublisher.publish(event);
    }

}

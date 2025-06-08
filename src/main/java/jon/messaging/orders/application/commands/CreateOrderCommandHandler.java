package jon.messaging.orders.application.commands;

import jon.messaging.orders.domain.Order;
import jon.messaging.orders.domain.OrderCreatedEvent;
import jon.messaging.orders.domain.OrderSpringJpaRepo;
import jon.messaging.orders.infra.RabbitPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateOrderCommandHandler {
    private final OrderSpringJpaRepo repo;
    private final ApplicationEventPublisher events;
    private final RabbitPublisher rabbitPublisher;

    @Transactional
    public void handle(CreateOrderCommand command) {
        var orderId = java.util.UUID.randomUUID();

        Order order = Order.Factory.create(Order.Id.of(UUID.randomUUID()), command.productId());
        repo.save(order);

        var event = new OrderCreatedEvent(orderId);

        //Publish in two brokers
        events.publishEvent(event);
        rabbitPublisher.publishOrderCreated(event);
    }

    public static record CreateOrderCommand(UUID orderId, String productId) {
    }
}

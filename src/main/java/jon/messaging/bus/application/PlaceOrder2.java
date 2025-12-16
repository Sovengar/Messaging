package jon.messaging.bus.application;

import jon.messaging.bus.old.commands.PlaceOrderCommand2;
import jon.messaging._shared.Order;
import jon.messaging._shared.OrderRepo;
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

    @Transactional
    public UUID handle(PlaceOrderCommand2 command) {
        log.info("BEGIN PlaceOrder");

        var orderId = command.orderId() != null ? command.orderId() : UUID.randomUUID();
        Order order = Order.Factory.create(Order.Id.of(orderId), command.productId());
        repo.save(order);

        log.info("END PlaceOrder");

        return orderId;
    }
}

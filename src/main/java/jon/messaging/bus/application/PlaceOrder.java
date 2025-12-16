package jon.messaging.bus.application;

import jakarta.annotation.Nullable;
import jon.messaging._shared.Order;
import jon.messaging._shared.OrderRepo;
import jon.messaging.bus.Command;
import jon.messaging.bus.CommandBus;
import jon.messaging.bus.CommandHandler;
import jon.messaging.bus.RequestBus;
import jon.messaging.bus.old.OldCommandBus;
import jon.messaging.bus.old.commands.PlaceOrderCommand2;
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
@RequestMapping("/bus/orders")
@RequiredArgsConstructor
class PlaceOrderBusController {
    private final OldCommandBus oldCommandBus;
    private final CommandBus commandBus;
    private final RequestBus requestBus;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody PlaceOrderRequest request) {
        commandBus.dispatch(new PlaceOrder.PlaceOrderCommand(request.orderId(), request.productId()));
        oldCommandBus.dispatch(new PlaceOrderCommand2(request.orderId(), request.productId()));
        requestBus.dispatch(new PlaceOrder.PlaceOrderCommand(request.orderId(), request.productId()));
        return ResponseEntity.ok(request.orderId().toString());
    }

    record PlaceOrderRequest(String productId, UUID orderId) { }
}

@Component
@RequiredArgsConstructor
@Slf4j
public class PlaceOrder implements CommandHandler<PlaceOrder.PlaceOrderCommand, UUID> {
    private final OrderRepo repo;

    @Transactional
    public UUID handle(PlaceOrderCommand command) {
        log.info("BEGIN PlaceOrder");

        var orderId = command.orderId() != null ? command.orderId() : UUID.randomUUID();
        Order order = Order.Factory.create(Order.Id.of(orderId), command.productId());
        repo.save(order);

        log.info("END PlaceOrder");

        return orderId;
    }

    public record PlaceOrderCommand(@Nullable UUID orderId, String productId) implements Command {
    }
}

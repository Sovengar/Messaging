package jon.messaging.orders.infra;

import jon.messaging.orders.application.commands.CreateOrderCommandHandler;
import jon.messaging.orders.domain.Order;
import jon.messaging.orders.application.queries.GetOrderByIdQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
class Endpoints {
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody CreateOrderRequest request) {
        commandBus.dispatch(new CreateOrderCommandHandler.CreateOrderCommand(request.orderId(), request.productId()));
        return ResponseEntity.ok(request.orderId().toString());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(queryBus.dispatch(new GetOrderByIdQueryHandler.GetOrderByIdQuery(id)));
    }

    record CreateOrderRequest(String productId, UUID orderId) { }
}

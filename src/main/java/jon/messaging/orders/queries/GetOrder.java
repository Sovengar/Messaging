package jon.messaging.orders.queries;

import jon.messaging.orders.domain.Order;
import jon.messaging.orders.domain.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
class GetOrderByIdHttpController {
    private final GetOrder getOrder;

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(getOrder.query(id));
    }
}

@Component
@RequiredArgsConstructor
public class GetOrder {
    private final OrderRepo repository;

    public Order query(UUID id) {
        return repository.findById(id).orElseThrow();
    }
}


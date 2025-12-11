package jon.messaging.orders.application.queries;

import jon.messaging.orders.domain.Order;
import jon.messaging.orders.domain.OrderSpringJpaRepo;
import jon.messaging.shared.messaging.QueryBus;
import jon.messaging.shared.messaging.queries.GetOrderByIdQuery;
import jon.messaging.shared.messaging.queries.GetOrdersSearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
class GetOrderByIdHttpController {
    private final QueryBus queryBus;

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(queryBus.send(new GetOrderByIdQuery(id)));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Order>> searchOrders(@RequestBody GetOrdersSearchCriteria filters) {
        return ResponseEntity.ok(queryBus.send(new GetOrdersSearchCriteria(filters.productId())));
    }
}

@Component
@RequiredArgsConstructor
public class GetOrder {
    private final OrderSpringJpaRepo repository;

    public Order handle(GetOrderByIdQuery query) {
        return repository.findById(query.id()).orElseThrow();
    }


}


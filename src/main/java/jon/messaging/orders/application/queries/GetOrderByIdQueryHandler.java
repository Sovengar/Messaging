package jon.messaging.orders.application.queries;

import jon.messaging.orders.domain.Order;
import jon.messaging.orders.domain.OrderSpringJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetOrderByIdQueryHandler {

    private final OrderSpringJpaRepo repository;

    public Order handle(GetOrderByIdQuery query) {
        return repository.findById(query.id()).orElseThrow();
    }

    public record GetOrderByIdQuery(UUID id) { }
}


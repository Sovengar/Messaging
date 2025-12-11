package jon.messaging.shared.messaging.queries;

import jon.messaging.orders.domain.Order;

import java.util.UUID;

public record GetOrderByIdQuery(UUID id) implements Query<Order> {
}

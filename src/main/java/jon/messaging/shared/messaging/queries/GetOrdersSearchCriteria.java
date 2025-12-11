package jon.messaging.shared.messaging.queries;

import jon.messaging.orders.domain.Order;

import java.util.List;

public record GetOrdersSearchCriteria(String productId) implements Query<List<Order>> {
}

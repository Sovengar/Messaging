package jon.messaging.orders.infra;

import jon.messaging.orders.domain.Order;
import jon.messaging.orders.application.queries.GetOrderByIdQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryBus {
    private final GetOrderByIdQueryHandler handler;

    public Order dispatch(GetOrderByIdQueryHandler.GetOrderByIdQuery query) {
        return handler.handle(query);
    }
}


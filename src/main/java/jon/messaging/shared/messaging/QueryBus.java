package jon.messaging.shared.messaging;

import jon.messaging.orders.application.queries.GetOrder;
import jon.messaging.orders.domain.Order;
import jon.messaging.shared.messaging.queries.GetOrderByIdQuery;
import jon.messaging.shared.messaging.queries.GetOrdersSearchCriteria;
import jon.messaging.shared.messaging.queries.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryBus {
    private final GetOrder getOrder;

    public <R> R send(Query<R> query){
        return (R) switch(query){
            case GetOrderByIdQuery q -> (R) getOrder.handle(q);
            case GetOrdersSearchCriteria(String productId) -> null;
        };
    }



}


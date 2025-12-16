package jon.messaging.events.events;

import jon.messaging.events.infra.rabbitmq.RabbitConfig;
import org.springframework.modulith.events.Externalized;

//IntegrationEvent
@Externalized(target = RabbitConfig.ORDERS_Q)
public class OrderCreated {

    private String orderId;

    public OrderCreated(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}

package jon.messaging.shared.messaging;

import jon.messaging.shared.messaging.events.OrderPlaced;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitPublisher {
    private final RabbitTemplate rabbitTemplate;

    //TODO Not needed with @Externalized
    public void publish(OrderPlaced event) {
        rabbitTemplate.convertAndSend(RabbitConfig.ORDERS_Q, "orders.placed", event);
    }
}

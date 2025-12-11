package jon.messaging.shared.messaging.events;

import jon.messaging.shared.messaging.RabbitConfig;
import org.springframework.modulith.events.Externalized;

//IntegrationEvent
@Externalized(target = RabbitConfig.ORDERS_Q)
class OrderCreated {
}

package jon.messaging.shared.domain.events;

import jon.messaging.shared.infra.rabbitmq.RabbitConfig;
import org.springframework.modulith.events.Externalized;

//IntegrationEvent
@Externalized(target = RabbitConfig.ORDERS_Q)
class OrderCreated {
}

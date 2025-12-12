package jon.messaging.orders.application.events;

import jon.messaging.shared.domain.events.OrderPlaced;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class OrderPlacedHandler {

    //Durable, saved to event_publication table
    @ApplicationModuleListener
    void handleOrderPlacedAsyncDurable(OrderPlaced event) {
        log.info("📥 Async Durable: Order created -> {}", event.orderId());
        //Useful to do small unit of work, like sending email
    }

    //TODO READ THE MANUALLY SENT RABBITMQ MSG
}

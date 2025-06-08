package jon.messaging.orders.domain;

import java.util.UUID;
import java.io.Serializable;

public record OrderCreatedEvent(UUID orderId) implements Serializable {}

package jon.messaging.events.events;

import java.util.UUID;
import java.io.Serializable;

public record OrderPlaced(UUID orderId) implements Serializable {}

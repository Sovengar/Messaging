package jon.messaging.shared.messaging.events;

import java.util.UUID;
import java.io.Serializable;

public record OrderPlaced(UUID orderId) implements Serializable {}

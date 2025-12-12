package jon.messaging.shared.domain.events;

import java.util.UUID;
import java.io.Serializable;

public record OrderPlaced(UUID orderId) implements Serializable {}

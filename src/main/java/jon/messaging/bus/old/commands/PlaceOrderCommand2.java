package jon.messaging.bus.old.commands;

import java.util.UUID;

public record PlaceOrderCommand2(UUID orderId, String productId) implements OldCommand<Void> {
}

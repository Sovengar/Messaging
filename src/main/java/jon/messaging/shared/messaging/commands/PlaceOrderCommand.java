package jon.messaging.shared.messaging.commands;

import java.util.UUID;

public record PlaceOrderCommand(UUID orderId, String productId) implements Command, jon.messaging.two.Command {
}

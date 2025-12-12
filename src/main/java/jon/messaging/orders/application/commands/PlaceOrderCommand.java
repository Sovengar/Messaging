package jon.messaging.orders.application.commands;


import jakarta.annotation.Nullable;
import jon.messaging.shared.infra.bus.Command;

import java.util.UUID;

public record PlaceOrderCommand(@Nullable UUID orderId, String productId) implements Command {
}

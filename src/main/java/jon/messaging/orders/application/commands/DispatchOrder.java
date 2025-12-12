package jon.messaging.orders.application.commands;

import jon.messaging.shared.infra.bus.CommandHandler;
import jon.messaging.shared.infra.bus.ValueCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
class DispatchOrder implements CommandHandler<ValueCommand<UUID>, Void> {
    @Override
    public Void handle(ValueCommand<UUID> command) {
        var id = command.getValue();
        log.info("Dispatching order {}", command.getValue());
        //...
        log.info("Order dispatched {}", id);
        return null;
    }
}

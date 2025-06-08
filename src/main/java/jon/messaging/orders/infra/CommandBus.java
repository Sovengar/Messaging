package jon.messaging.orders.infra;

import jon.messaging.orders.application.commands.CreateOrderCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandBus {
    private final CreateOrderCommandHandler handler;

    public void dispatch(CreateOrderCommandHandler.CreateOrderCommand command) {
        handler.handle(command);
    }
}


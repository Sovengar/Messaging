package jon.messaging.shared.messaging;

import jon.messaging.orders.application.commands.PlaceOrder;
import jon.messaging.shared.messaging.commands.Command;
import jon.messaging.shared.messaging.commands.PlaceOrderCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandBus {
    private final PlaceOrder handler;

//    public void dispatch(PlaceOrderCommand command) {
//        handler.handle(command);
//    }

    public void send(Command command) {
        switch (command) {
            case PlaceOrderCommand c -> handler.handle(c);
        }
        //case Withdraw(String accountId, double amount) -> bankService.withdraw(accountId, amount);
    }

}


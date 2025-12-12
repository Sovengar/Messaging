package jon.messaging.old;

import jon.messaging.old.commands.OldCommand;
import jon.messaging.old.commands.PlaceOrderCommand2;
import jon.messaging.orders.application.commands.PlaceOrder2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

//Me obliga a que el Handler sea public
//Me obliga a que los commands, al ser sealed, esten todos juntos con la interfaz "Command"
//Poco relevante: Se va a ver muy bloated y con problemas de merge este fichero y la sealed interfaz "Command"

@Component
@RequiredArgsConstructor
public class CommandBusWithPatternMatching implements OldCommandBus {
    private final PlaceOrder2 handler;

    @Override
    public <R> R dispatch(OldCommand oldCommand) {
        return switch (oldCommand) {
            case PlaceOrderCommand2 c -> (R) handler.handle(c);
        };
        //case Withdraw(String accountId, double amount) -> bankService.withdraw(accountId, amount);
    }
}
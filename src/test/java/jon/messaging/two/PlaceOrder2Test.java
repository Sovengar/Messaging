package jon.messaging.two;

import jon.messaging.EnableTestContainers;
import jon.messaging.shared.messaging.CommandBus;
import jon.messaging.shared.messaging.commands.PlaceOrderCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnableTestContainers
class PlaceOrder2Test {
    @Autowired
    InMemoryCommandBus commandBus;

    @Autowired
    CommandBus commandBus2;

    @Test
    void a(){
        commandBus.dispatch(new PlaceOrderCommand(UUID.randomUUID(), "123"));
        commandBus2.send(new PlaceOrderCommand(UUID.randomUUID(), "123"));
    }

}
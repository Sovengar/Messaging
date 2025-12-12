package jon.messaging.orders.application;

import jon.messaging.EnableTestContainers;
import jon.messaging.old.OldCommandBus;
import jon.messaging.old.commands.PlaceOrderCommand2;
import jon.messaging.orders.application.commands.PlaceOrderCommand;
import jon.messaging.shared.infra.bus.CommandBus;
import jon.messaging.shared.infra.bus.RequestBus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@EnableTestContainers
class PlaceOrderIT {
    @Autowired
    CommandBus commandBus;

    @Autowired
    OldCommandBus oldCommandBus;

    @Autowired
    RequestBus requestBus;

    @Test
    void a(){
        var orderId = UUID.randomUUID();

        var result1 = (UUID) commandBus.dispatch(new PlaceOrderCommand(orderId, "123"));
        var result2 = (UUID) oldCommandBus.dispatch(new PlaceOrderCommand2(orderId, "123"));
        var result3 = (UUID) requestBus.dispatch(new PlaceOrderCommand(orderId, "123"));

        assertThat(result1).isEqualTo(orderId);
        assertThat(result2).isEqualTo(orderId);
        assertThat(result3).isEqualTo(orderId);
    }
}
package jon.messaging.bus.orders.application;

import jon.messaging._config.EnableTestContainers;
import jon.messaging.bus.application.PlaceOrder;
import jon.messaging.bus.old.OldCommandBus;
import jon.messaging.bus.old.commands.PlaceOrderCommand2;
import jon.messaging.bus.CommandBus;
import jon.messaging.bus.RequestBus;
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

        var result1 = (UUID) commandBus.dispatch(new PlaceOrder.PlaceOrderCommand(orderId, "123"));
        var result2 = (UUID) oldCommandBus.dispatch(new PlaceOrderCommand2(orderId, "123"));
        var result3 = (UUID) requestBus.dispatch(new PlaceOrder.PlaceOrderCommand(orderId, "123"));

        assertThat(result1).isEqualTo(orderId);
        assertThat(result2).isEqualTo(orderId);
        assertThat(result3).isEqualTo(orderId);
    }
}

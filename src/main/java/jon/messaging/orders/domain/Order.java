package jon.messaging.orders.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jon.messaging.shared.domain.events.OrderPlaced;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.util.UUID;


@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class Order extends AbstractAggregateRoot<Order> {

    @EmbeddedId
    private Id id;

    private String productId;
    private Status status;
    //private int quantity;
    //private double price;
    //private String status;
    //private String customerId;

    public Order placeAndPublishEvent(){
        status = Status.PLACED;
        //Method coming from AbstractAggregateRoot, Spring Data will publish the event when we use repo.save()
        //Example: repo.save(order.placeAndPublishEvent())
        registerEvent(new OrderPlaced(this.id.orderId));
        return this;
    }

    //Idk, mutates an object and returns another object... Maybe specify more in the name the side effect...
    public OrderPlaced place(){
        status = Status.PLACED;
        return new OrderPlaced(this.id.orderId);
    }

    @Getter
    enum Status {PLACED, CANCELLED, COMPLETED}

    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    @Getter
    public static class Id {
        private UUID orderId;

        public static Id of(UUID orderId){
            return new Id(orderId);
        }
    }

    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static class Factory {
        public static Order create(Id orderId, String productId){
            return new Order(orderId, productId, Status.PLACED);
        }
    }
}

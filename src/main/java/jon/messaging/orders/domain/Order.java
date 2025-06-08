package jon.messaging.orders.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class Order {

    @EmbeddedId
    private Id id;

    private String productId;
    //private int quantity;
    //private double price;
    //private String status;
    //private String customerId;

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

    public static class Factory {
        public static Order create(Id orderId, String productId){
            return new Order(orderId, productId);
        }
    }
}

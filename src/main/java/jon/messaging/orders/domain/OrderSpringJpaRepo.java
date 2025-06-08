package jon.messaging.orders.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

//Since JPA is pretty clean, used as a domain repo
public interface OrderSpringJpaRepo extends JpaRepository<Order, UUID> {
}

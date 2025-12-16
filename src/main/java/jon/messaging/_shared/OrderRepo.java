package jon.messaging._shared;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

//Since JPA is pretty clean, used as a domain repo
public interface OrderRepo extends JpaRepository<Order, UUID> {
}

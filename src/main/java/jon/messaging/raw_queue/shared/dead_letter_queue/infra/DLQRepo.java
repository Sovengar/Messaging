package jon.messaging.raw_queue.shared.dead_letter_queue.infra;

import jon.messaging.raw_queue.shared.dead_letter_queue.domain.DeadLetterQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface DLQRepo {
    //List<DeadLetterQueue> findAll(); //Could throw an OutOfMemory if it comes to a massive influx of failed messages

    Optional<DeadLetterQueue> findById(UUID messageId);

    //Avoid List because it could throw OutOfMemory if it comes to a massive influx of failed messages
    Stream<DeadLetterQueue> getTimeoutMessages();

    //Avoid List because it could throw OutOfMemory if it comes to a massive influx of failed messages
    Stream<DeadLetterQueue> streamAll();

    void create(DeadLetterQueue deadLetterQueue);

    void update(DeadLetterQueue deadLetterQueue);

    void delete(UUID messageId);
}

@Repository
@RequiredArgsConstructor
class DLQPosgreRepo implements DLQRepo {
    private final DLQSpringJPARepo dlqSpringJPARepo;

//    @Override
//    public List<DeadLetterQueue> findAll() {
//        return dlqSpringJPARepo.findAll();
//    }

    @Override
    public Optional<DeadLetterQueue> findById(UUID messageId) {
        return dlqSpringJPARepo.findById(messageId);
    }

    @Override
    public Stream<DeadLetterQueue> streamAll() {
        return dlqSpringJPARepo.streamAll();
    }

    @Override
    public Stream<DeadLetterQueue> getTimeoutMessages() {
        return dlqSpringJPARepo.streamTimeoutMessages();
    }

    @Override
    public void create(DeadLetterQueue deadLetterQueue) {
        dlqSpringJPARepo.save(deadLetterQueue);
    }

    @Override
    public void update(final DeadLetterQueue deadLetterQueue) {
        dlqSpringJPARepo.save(deadLetterQueue);
    }

    @Override
    public void delete(UUID messageId) {
        dlqSpringJPARepo.deleteById(messageId);
    }
}

interface DLQSpringJPARepo extends JpaRepository<DeadLetterQueue, UUID> {
    @Query("SELECT d FROM DeadLetterQueue d ORDER BY d.arrivedAt DESC")
    Stream<DeadLetterQueue> streamAll();

    @Query("SELECT d.messageId FROM DeadLetterQueue d WHERE d.origin = :origin ORDER BY d.arrivedAt DESC")
    List<UUID> findIdsByOrigin(String origin);

    @Query("SELECT d FROM DeadLetterQueue d WHERE d.error LIKE '%timeout%' OR d.error LIKE '%504%' OR d.error LIKE '%El servei no ha respost a temps%' ORDER BY d.arrivedAt DESC")
    Stream<DeadLetterQueue> streamTimeoutMessages();
}

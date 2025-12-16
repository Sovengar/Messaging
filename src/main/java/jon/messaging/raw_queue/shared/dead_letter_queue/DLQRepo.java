package jon.messaging.raw_queue.shared.dead_letter_queue;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

public interface DLQRepo {
    List<DeadLetterQueue> findAll(); //Could throw an OutOfMemory if it comes to a massive influx of failed messages

    void create(DeadLetterQueue deadLetterQueue);

    void delete(UUID messageId);
}

@Repository
@RequiredArgsConstructor
class DLQPosgreRepo implements DLQRepo {
    private final DLQSpringJPARepo dlqSpringJPARepo;

    @Override
    public List<DeadLetterQueue> findAll() {
        return dlqSpringJPARepo.findAll();
    }

    @Override
    public void create(DeadLetterQueue deadLetterQueue) {
        dlqSpringJPARepo.save(deadLetterQueue);
    }

    @Override
    public void delete(UUID messageId) {
        dlqSpringJPARepo.deleteById(messageId);
    }
}

interface DLQSpringJPARepo extends JpaRepository<DeadLetterQueue, UUID> { }

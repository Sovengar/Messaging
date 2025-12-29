package jon.messaging.raw_queue.queues.character_queue.infra;

import jon.messaging.raw_queue.queues.character_queue.CharacterQueue;
import jon.messaging.raw_queue.shared.abstract_queue.QueueSpringJpaRepo;

import java.util.List;

public interface CharacterQueueSpringJpaRepo extends QueueSpringJpaRepo<CharacterQueue, Long> {
    List<CharacterQueue> findTop20ByOrderByInternalIdDesc();
}

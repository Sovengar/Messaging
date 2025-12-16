package jon.messaging.raw_queue.queues;

import jakarta.persistence.EntityManager;
import jon.messaging.raw_queue.characters.character_queue.CharacterQueue;
import jon.messaging.raw_queue.characters.character_queue.infra.CharacterQueueSpringJpaRepo;
import jon.messaging.raw_queue.queues.product_queue.ProductQueue;
import jon.messaging.raw_queue.queues.product_queue.ProductQueueSpringJpaRepo;
import jon.messaging.raw_queue.shared.abstract_queue.QueuePostgreRepo;
import jon.messaging.raw_queue.shared.abstract_queue.QueueRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class QueueRepoConfig {
    @Bean
    public QueueRepo<CharacterQueue, Long> characterQueueRepository(EntityManager entityManager, CharacterQueueSpringJpaRepo characterQueueJpaRepo) {
        return new QueuePostgreRepo<>(entityManager, characterQueueJpaRepo, CharacterQueue.class);
    }

    @Bean
    public QueueRepo<ProductQueue, Long> productQueueRepository(EntityManager entityManager, ProductQueueSpringJpaRepo productQueueJpaRepo) {
        return new QueuePostgreRepo<>(entityManager, productQueueJpaRepo, ProductQueue.class);
    }
}


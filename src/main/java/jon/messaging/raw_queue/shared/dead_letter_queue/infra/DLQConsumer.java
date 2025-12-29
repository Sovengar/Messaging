package jon.messaging.raw_queue.shared.dead_letter_queue.infra;

import jakarta.persistence.EntityManager;
import jon.messaging.raw_queue.shared.dead_letter_queue.application.RetryProcessing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
class DLQConsumer {
    private final RetryProcessing retryProcessing;
    private final DLQRepo repo;
    private final EntityManager entityManager;

    @Transactional
    @Scheduled(fixedDelay = 3600000) // 1 Hour
    public void retryTimeoutMessages(){
        log.info("BEGIN Retrying timeout messages");

        try (var timeoutMessages = repo.getTimeoutMessages()) {
            timeoutMessages.forEach(dlqMsg -> {
                try {
                    log.trace("Retrying message {}", dlqMsg.getMessageId());
                    retryProcessing.handle(dlqMsg.getMessageId());
                    entityManager.flush(); //Commit on DB before detaching
                    entityManager.detach(dlqMsg); // Keep memory clean to avoid memoryException
                } catch (Exception e) {
                    entityManager.detach(dlqMsg); // Keep memory clean to avoid memoryException
                    log.trace("Timeout message {} could not be retried: {}", dlqMsg.getMessageId(), e.getMessage(), e);
                }
            });
        }

        log.info("END Retrying timeout messages");
    }
}

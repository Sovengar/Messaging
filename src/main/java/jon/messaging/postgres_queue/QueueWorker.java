package jon.messaging.postgres_queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class QueueWorker {

    private static final Logger log = LoggerFactory.getLogger(QueueWorker.class);
    private final PgmqService pgmqService;
    private static final String QUEUE_NAME = "my_job_queue";
    private static final String DLQ_NAME = "my_job_queue_dlq";
    private static final int MAX_RETRIES = 3;

    public QueueWorker(PgmqService pgmqService) {
        this.pgmqService = pgmqService;
        // ensure queues exist on startup
        pgmqService.createQueue(QUEUE_NAME);
        pgmqService.createQueue(DLQ_NAME);
    }

    @Scheduled(fixedDelay = 1000)
    public void process() {
        // Read with VT=30s
        Optional<PgmqMessage> msgOpt = pgmqService.readMessage(QUEUE_NAME, 30);

        if (msgOpt.isPresent()) {
            PgmqMessage msg = msgOpt.get();
            log.info("Received message: {} (Read count: {})", msg.message(), msg.read_ct());

            if (msg.read_ct() > MAX_RETRIES) {
                log.warn("Message {} exceeded max retries. Moving to DLQ.", msg.msg_id());
                // Move to DLQ: Send to DLQ, then delete from main queue.
                // Note: ideally this should be atomic, but PGMQ doesn't have a single 'move'
                // function yet.
                // We could wrap in transaction? PGMQ uses its own transactions internally,
                // but calling two pgmq functions in one verifyable transaction works if we are
                // in a @Transactional method.
                moveToDlq(msg);
                return;
            }

            try {
                // Simulate processing
                doWork(msg.message());

                // Success: archive
                pgmqService.archiveMessage(QUEUE_NAME, msg.msg_id());
                log.info("Message {} processed and archived.", msg.msg_id());
            } catch (Exception e) {
                log.error("Failed to process message {}. It will become visible again after VT.", msg.msg_id(), e);
                // Do nothing, let VT expire.
            }
        }
    }

    private void moveToDlq(PgmqMessage msg) {
        pgmqService.sendMessage(DLQ_NAME, msg.message());
        pgmqService.deleteMessage(QUEUE_NAME, msg.msg_id());
    }

    private void doWork(String body) {
        if (body.contains("fail")) {
            throw new RuntimeException("Simulated failure");
        }
        // OK
    }
}

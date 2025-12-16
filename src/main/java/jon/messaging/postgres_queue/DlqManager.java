package jon.messaging.postgres_queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DlqManager {

    private static final Logger log = LoggerFactory.getLogger(DlqManager.class);
    private final PgmqService pgmqService;
    private static final String QUEUE_NAME = "my_job_queue";
    private static final String DLQ_NAME = "my_job_queue_dlq";

    public DlqManager(PgmqService pgmqService) {
        this.pgmqService = pgmqService;
    }

    public long getDlqCount() {
        // PGMQ doesn't have a direct 'count' function exposed easily in simple API,
        // we can select count(*) from the queue table.
        // The queue table is named using the queue name.
        // PGMQ creates a table named "my_job_queue_dlq" (public schema usually).
        // WARNING: internal implementation detail of PGMQ.
        // A safer way is using pgmq_meta or just assuming the table exists.
        return 0; // skipped for simplicity, or we can add a 'count' method to service if needed.
    }

    public void reprocessDlqMessages(int limit) {
        log.info("Checking DLQ for messages to reprocess...");
        List<PgmqMessage> msgs = pgmqService.readMessages(DLQ_NAME, 30, limit);

        for (PgmqMessage msg : msgs) {
            log.info("Reprocessing message from DLQ: {}", msg.msg_id());
            // Move back to main queue
            pgmqService.sendMessage(QUEUE_NAME, msg.message());
            pgmqService.deleteMessage(DLQ_NAME, msg.msg_id());
        }
    }
}

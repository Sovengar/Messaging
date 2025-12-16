package jon.messaging.postgres_queue;

import java.time.OffsetDateTime;

public record PgmqMessage(long msg_id, long read_ct, OffsetDateTime enqueued_at, OffsetDateTime vt,
                          String message) {
}

package jon.messaging.raw_queue.shared.dead_letter_queue.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dead_letter_queue")
@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class DeadLetterQueue {
    @Id
    private UUID messageId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String data;
    private LocalDateTime arrivedAt;

    private String fromQueue;
    @Enumerated(EnumType.STRING)
    private OriginType origin;
    private String error;

    public enum OriginType {
        FRONTEND,
        EXTERNAL_CLIENT,
        APPLICATION,
        CRON
    }

    public void updateError(String error) {
        this.error = error;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Factory {
        public static DeadLetterQueue create(
                UUID messageId,
                String data,
                LocalDateTime arrivedAt,
                String fromQueue,
                OriginType origin,
                String error
        ) {
            return new DeadLetterQueue(messageId, data, arrivedAt, fromQueue, origin, error);
        }
    }
}

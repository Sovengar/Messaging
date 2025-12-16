package jon.messaging.postgres_queue;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PgmqService {
    private final JdbcClient jdbcClient;

    public PgmqService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Transactional
    public void createQueue(String queueName) {
        // Idempotent creation isn't built-in to 'create', but we can check existence or
        // just catch error.
        // PGMQ has 'create_if_not_exists' in newer versions, or we can check pg_tables.
        // For simplicity in this demo, we'll try to create and ignore if it exists or
        // rely on a check.
        // Better: create unlogged or normal? Default is normal.
        try {
            jdbcClient.sql("SELECT pgmq.create(:q)")
                    .param("q", queueName)
                    .query(Object.class).list();
        } catch (Exception e) {
            // In a real app, check if error is "relation already exists"
            // For prototype: assume it might exist.
        }
    }

    @Transactional
    public Long sendMessage(String queueName, String jsonMessage) {
        return jdbcClient.sql("SELECT * FROM pgmq.send(:q, :msg::jsonb)")
                .param("q", queueName)
                .param("msg", jsonMessage)
                .query(Long.class)
                .single();
    }

    @Transactional
    public Optional<PgmqMessage> readMessage(String queueName, int visibilityTimeoutSeconds) {
        List<PgmqMessage> msgs = jdbcClient.sql("SELECT * FROM pgmq.read(:q, :vt, 1)")
                .param("q", queueName)
                .param("vt", visibilityTimeoutSeconds)
                .query(PgmqMessage.class)
                .list();

        return msgs.isEmpty() ? Optional.empty() : Optional.of(msgs.get(0));
    }

    @Transactional
    public List<PgmqMessage> readMessages(String queueName, int visibilityTimeoutSeconds, int limit) {
        return jdbcClient.sql("SELECT * FROM pgmq.read(:q, :vt, :limit)")
                .param("q", queueName)
                .param("vt", visibilityTimeoutSeconds)
                .param("limit", limit)
                .query(PgmqMessage.class)
                .list();
    }

    @Transactional
    public void archiveMessage(String queueName, long msgId) {
        jdbcClient.sql("SELECT pgmq.archive(:q, :id)")
                .param("q", queueName)
                .param("id", msgId)
                .query(Object.class).list();
    }

    @Transactional
    public void deleteMessage(String queueName, long msgId) {
        jdbcClient.sql("SELECT pgmq.delete(:q, :id)")
                .param("q", queueName)
                .param("id", msgId)
                .query(Object.class).list();
    }
}

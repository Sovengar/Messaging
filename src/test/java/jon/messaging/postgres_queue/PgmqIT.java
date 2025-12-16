package jon.messaging.postgres_queue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
class PgmqIntegrationTest {

    // Must match the image we plan to use in prod/compose if possible
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("quay.io/tembo/pg16-pgmq:latest")
                    .asCompatibleSubstituteFor("postgres"));

    @Autowired
    private PgmqService pgmqService;

    @Test
    void testQueueFlow() {
        String queueName = "test_queue";
        pgmqService.createQueue(queueName);

        // 1. Send message
        long msgId = pgmqService.sendMessage(queueName, "{\"type\":\"hello\"}");
        assertThat(msgId).isGreaterThan(0);

        // 2. Read message
        Optional<PgmqMessage> msgOpt = pgmqService.readMessage(queueName, 30);
        assertThat(msgOpt).isPresent();
        PgmqMessage msg = msgOpt.get();
        assertThat(msg.message()).contains("hello");
        assertThat(msg.msg_id()).isEqualTo(msgId);

        // 3. Archive (Ack)
        pgmqService.archiveMessage(queueName, msgId);

        // 4. Verify no longer visible
        Optional<PgmqMessage> check = pgmqService.readMessage(queueName, 30);
        assertThat(check).isEmpty();
    }

    @Test
    void testVisibilityTimeout() throws InterruptedException {
        String queueName = "vt_queue";
        pgmqService.createQueue(queueName);

        long msgId = pgmqService.sendMessage(queueName, "{\"val\":\"vt-test\"}");

        // Read with short VT (1 second)
        Optional<PgmqMessage> msg1 = pgmqService.readMessage(queueName, 1);
        assertThat(msg1).isPresent();

        // Immediately try to read again -> should be empty (invisible)
        Optional<PgmqMessage> msg2 = pgmqService.readMessage(queueName, 1);
        assertThat(msg2).isEmpty();

        // Wait for VT to expire
        await().atMost(Duration.ofSeconds(3)).until(() -> {
            return pgmqService.readMessage(queueName, 1).isPresent();
        });

        // Should be visible again
        Optional<PgmqMessage> msg3 = pgmqService.readMessage(queueName, 1);
        assertThat(msg3).isPresent();
        assertThat(msg3.get().msg_id()).isEqualTo(msgId);
    }
}

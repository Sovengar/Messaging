package jon.messaging.raw_queue.shared.dead_letter_queue.application;


import jon.messaging.raw_queue.shared.dead_letter_queue.infra.DLQRepo;
import jon.messaging.raw_queue.shared.dead_letter_queue.domain.DeadLetterQueue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static jon.messaging.raw_queue.shared.dead_letter_queue.infra.JsonUtils.removeEnclosingQuotes;
import static jon.messaging.raw_queue.shared.dead_letter_queue.infra.JsonUtils.simulateDbEscape;

@RestController
@RequestMapping("/dead-letter-queue")
@RequiredArgsConstructor
@Slf4j
@Validated
class RetryProcessingHttpController {
    private final RetryProcessing retry;

    @PutMapping("/retry/{messageId}")
    public ResponseEntity<Object> retryMessage(final @PathVariable UUID messageId) {
        Assert.notNull(messageId, "Message ID cannot be null");
        var instrumentation = new RetryProcessingInstrumentation(messageId);

        try {
            instrumentation.logStartProcessing();
            retry.handle(messageId);
            instrumentation.logFinishedProcessing();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            instrumentation.logFinishedProcessing();
            return ResponseEntity.internalServerError().body(e.getMessage() + " - " + e.getCause() + " - " + e.getLocalizedMessage());
        }
    }

    @PutMapping("/retry/fixed/{messageId}")
    public ResponseEntity<Object> retryMessageWithFixedPayload(@PathVariable final UUID messageId, @Valid @RequestBody final MessageWithFixedBody request) {
        Assert.notNull(messageId, "Message ID cannot be null");
        var instrumentation = new RetryProcessingInstrumentation(messageId);

        try {
            instrumentation.logStartProcessing();
            retry.handle(messageId, request.getNewPayload());
            instrumentation.logFinishedProcessing();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            instrumentation.logFinishedProcessing();
            return ResponseEntity.internalServerError().body(e.getMessage() + " - " + e.getCause() + " - " + e.getLocalizedMessage());
        }
    }

    @PutMapping("/retry/batch")
    public ResponseEntity<Void> retryMessages(@RequestBody List<UUID> messageIds) {
        Assert.notNull(messageIds, "Message IDs cannot be null");

        if(messageIds.size() > 1000) {
            throw new IllegalArgumentException("Quantity cannot be greater than 1000");
        }

        log.info("Retrying messages {}", messageIds);
        messageIds.forEach(retry::handle);
        log.info("Retried messages {}", messageIds);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/retry/last/{quantity}")
    public ResponseEntity<Void> retryLastMessages(@PathVariable final int quantity) {
        retry.handleLastMessages(quantity);
        return ResponseEntity.ok().build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor //For Jackson
    static class MessageWithFixedBody {
        @NotEmpty(message = "Payload cannot be empty")
        String newPayload;
    }
}

@Slf4j
@RequiredArgsConstructor
class RetryProcessingInstrumentation {
    private final UUID messageId;

    void logStartProcessing() {
        log.info("Retrying message {}", messageId);
    }

    void logFinishedProcessing() {
        log.info("Retried message {}", messageId);
    }
}

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryProcessing {
    private final DLQRepo repo;
    private final CaseQueueExecutor caseQueueExecutor;
    private final MarkAsProcessed markAsProcessed;

    public void handle(UUID messageId) {
        var msg = repo.findById(messageId).orElseThrow();
        var dataWithoutEnclosingQuotes = removeEnclosingQuotes(msg.getData());

        try {
            retry(msg, dataWithoutEnclosingQuotes);
        } catch (Exception e) {
            updateFailedDlqMsg(e, msg);
        }
    }

    @Transactional
    //PUBLIC FOR SPRING PROXY INJECTION OF @TRANSACTIONAL, SHALL NOT BE CONSUMED FROM OUTSIDE!!!
    public void retry(DeadLetterQueue msg, String data){
        caseQueueExecutor.handle(new CaseQueueMessage(msg.getMessageId(), data, DeadLetterQueue.OriginType.valueOf(msg.getOrigin())));
        markAsProcessed.markAsProcessed(msg.getMessageId());
    }

    public void handle(UUID messageId, String data) {
        var msg = repo.findById(messageId).orElseThrow();

        try {
            retry(msg, simulateDbEscape(data));
        } catch (Exception e) {
            updateFailedDlqMsg(e, msg);
        }
    }

    public void handleLastMessages(final int quantity) {
        if (quantity > 1000) {
            throw new IllegalArgumentException("Quantity cannot be greater than 1000");
        }

        try (Stream<DeadLetterQueue> stream = repo.streamAll()) {
            var lastMessages = stream.limit(quantity);

            lastMessages.forEach(msg -> {
                try {
                    retry(msg, removeEnclosingQuotes(msg.getData()));
                } catch (Exception e) {
                    updateFailedDlqMsg(e, msg);
                }
            });
        }
    }

    private void updateFailedDlqMsg(final Exception e, final DeadLetterQueue msg) {
        log.error("Error processing message {}", msg.getMessageId(), e);
        log.info("Updating error message for message {}", msg.getMessageId());
        msg.updateError(e.getMessage());
        repo.update(msg);
        throw new RuntimeException(e);
    }
}

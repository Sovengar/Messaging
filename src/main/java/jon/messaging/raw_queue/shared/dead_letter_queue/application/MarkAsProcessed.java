package jon.messaging.raw_queue.shared.dead_letter_queue.application;

import jon.messaging.raw_queue.shared.dead_letter_queue.infra.DLQRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dead-letter-queue")
@RequiredArgsConstructor
class MarkAsProcessed {
    private final DLQRepo repo;
    private final DeadLetterQueueHandler service;

    @PutMapping("/processed/{messageId}")
    public ResponseEntity<Void> markAsProcessed(final @PathVariable UUID messageId) {
        Assert.notNull(messageId, "Message ID cannot be null");

        service.markAsProcessed(messageId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/processed/batch")
    public ResponseEntity<Void> markAsProcessedBatch(@RequestBody List<UUID> messageIds) {
        Assert.notNull(messageIds, "Message IDs cannot be null");
        messageIds.forEach(service::markAsProcessed);
        return ResponseEntity.ok().build();
    }
}

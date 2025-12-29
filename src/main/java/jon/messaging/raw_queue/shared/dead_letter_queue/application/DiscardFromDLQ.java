package jon.messaging.raw_queue.shared.dead_letter_queue.application;

import jakarta.persistence.EntityManager;
import jon.messaging.raw_queue.shared.dead_letter_queue.infra.DLQRepo;
import jon.messaging.raw_queue.shared.dead_letter_queue.domain.DeadLetterQueue;
import jon.messaging.raw_queue.shared.dead_letter_queue.queries.SearchDeadMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@RestController
@RequestMapping("/dead-letter-queue")
@RequiredArgsConstructor
class DiscardFromDLQ {
    private final DLQRepo repo;
    private final EntityManager entityManager;
    private final SearchDeadMessages searchDeadMessages;

    @DeleteMapping("/discard/{messageId}")
    public ResponseEntity<Void> discardMessage(final @PathVariable UUID messageId) {
        Assert.notNull(messageId, "Message ID cannot be null");

        repo.delete(messageId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/discard/batch")
    @Transactional
    public ResponseEntity<Void> discardMessagesInBatch(final @RequestBody List<UUID> messageIds) {
        Assert.notNull(messageIds, "Please provide messages");
        Assert.isTrue(!messageIds.isEmpty(), "Please provide messages");

        if(messageIds.size() > 1000){
            throw new IllegalArgumentException("Quantity cannot be greater than 1000");
        }

        messageIds.forEach(repo::delete);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/discard/last/{quantity}")
    @Transactional
    public ResponseEntity<Void> discardLastMessages(@PathVariable final int quantity) {
        if(quantity > 1000){
            throw new IllegalArgumentException("Quantity cannot be greater than 1000");
        }

        try (Stream<DeadLetterQueue> stream = repo.streamAll()) {
            stream.limit(quantity).forEach(dlqMsg -> repo.delete(dlqMsg.getMessageId()));
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/discard/all")
    @Transactional
    public ResponseEntity<Void> discardAllMessages() {
        try (Stream<DeadLetterQueue> stream = repo.streamAll()) {
            stream.forEach(dlqMsg -> {
                repo.delete(dlqMsg.getMessageId());
                entityManager.flush(); //Commit on DB before detaching
                entityManager.detach(dlqMsg); // Keep memory clean to avoid memoryException
            });
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/discard/matching-filters")
    @Transactional
    public ResponseEntity<Void> discardMessagesMatchingFilters(final @RequestBody SearchDeadMessages.Filters filters) {
        Assert.notNull(filters, "Filters cannot be null");

        try (var streamOfDlqMessages = searchDeadMessages.streamAll(filters)) {
            streamOfDlqMessages.forEach(dlqMsg -> {
                repo.delete(dlqMsg.getMessageId());
                entityManager.flush(); //Commit on DB before detaching
                entityManager.detach(dlqMsg); // Keep memory clean to avoid memoryException
            });
        }

        return ResponseEntity.ok().build();
    }
}

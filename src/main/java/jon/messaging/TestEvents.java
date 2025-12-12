package jon.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional //Event has to be published in a transaction for @TransactionalEventListener to listen
class TestEvents {
    private final ApplicationEventPublisher publisher;

    @EventListener
    public void onStart(ApplicationReadyEvent event) throws InterruptedException {
      log.info("Registering user");
        publishEvents();
    }

    void publishEvents() throws InterruptedException {
        publisher.publishEvent(new UserRegisteredAsync(UUID.randomUUID())); //Async
        publisher.publishEvent(new UserRegisteredSync(UUID.randomUUID())); //Sync
    }

    //This happens in the same transaction. An error here rolls back the producer, care
    //Probably not suited for event notification.
    @EventListener
    void handleSync(UserRegisteredSync event) throws InterruptedException {
        // se ejecuta antes del commit y dentro de la misma transacción
        log.info("BEGIN ✅ Sync in-memory event: User registered -> {}", event.userId());
        Thread.sleep(5_000);
        log.info("END ✅ Sync in-memory event: User registered -> {}", event.userId());
    }

    //This happens in the same transaction. An error here rolls back the producer, care
    //Probably not suited for event notification.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW) //???????? NO DEBERIA ESTAR ESTO
    void handleSyncWithoutRollbackAfterFail(UserRegisteredSync event) throws InterruptedException {
        // se ejecuta antes del commit y dentro de la misma transacción
        log.info("BEGIN ✅ Sync in-memory [After Commit] event: User registered -> {}", event.userId());
        Thread.sleep(5_000);
        log.info("END ✅ Sync in-memory [After Commit] event: User registered -> {}", event.userId());
    }

    //Happens outside the transaction thread.
    //Not durable, all in memory.
    @Async
    @EventListener
    void handleAsyncInMemory(UserRegisteredSync event) throws InterruptedException {
        log.info("BEGIN 📥 Async in-memory: User registered -> {}", event.userId());
        Thread.sleep(5_000);
        log.info("END 📥 Async in-memory: User registered -> {}", event.userId());
    }

    //Happens outside the transaction thread.
    //Durable, saved to event_publication table
    @ApplicationModuleListener
    public void a(UserRegisteredAsync event) throws InterruptedException {
        log.info("BEGIN 📥 Async Durable: User registered -> {}", event.userId());

        //Check table event_publication while this is being processed.
        Thread.sleep(10_000);
        //Check table event_publication while this is being processed.

        log.info("END 📥 Async Durable: User registered -> {}", event.userId());
    }

    record UserRegisteredSync(UUID userId) {
    }

    record UserRegisteredAsync(UUID userId) {
    }
}

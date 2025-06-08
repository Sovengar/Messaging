package jon.messaging.orders;

import jon.messaging.orders.domain.OrderCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

class OrderCreatedHandler {
    @Component
    public static class OrderCreatedSyncHandler {

        //This happens in the same transaction, an error here rollbacks the producer, care
        @EventListener
        public void handle(OrderCreatedEvent event) {
            System.out.println("📥 Sync handler: Order created -> " + event.orderId());
        }
    }

    @Component
    public static class OrderCreatedAsyncInMemoryHandler {

        //Happens outside the transaction thread.
        //Not durable, all in memory.
        @Async
        @EventListener
        //Needed? @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void handle(OrderCreatedEvent event) {
            System.out.println("📥 Async in-memory: Order created -> " + event.orderId());
        }
    }

    @Component
    public static class OrderCreatedAsyncDurableHandler {

        @ApplicationModuleListener
        public void handle(OrderCreatedEvent event) {
            System.out.println("📥 Durable handler (AFTER_COMMIT): Order created -> " + event.orderId());
            // Aquí podrías notificar otro microservicio, enviar correo, etc.
        }
    }
}

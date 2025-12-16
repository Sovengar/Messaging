package jon.messaging.bus;

public interface CommandHandler<C extends Command, R> {
    R handle(C command);
}

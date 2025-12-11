package jon.messaging.two;

public interface CommandHandler<C extends Command, R> {
    R handle(C command);
}

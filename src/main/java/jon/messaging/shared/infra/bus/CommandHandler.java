package jon.messaging.shared.infra.bus;

public interface CommandHandler<C extends Command, R> {
    R handle(C command);
}

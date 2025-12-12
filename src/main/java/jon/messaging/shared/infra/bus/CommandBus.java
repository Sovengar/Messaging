package jon.messaging.shared.infra.bus;

public interface CommandBus {
    <R> R dispatch(Command command);
}

package jon.messaging.bus;

public interface CommandBus {
    <R> R dispatch(Command command);
}

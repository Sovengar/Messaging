package jon.messaging.two;

public interface CommandBus {
    <R> R dispatch(Command command);
}

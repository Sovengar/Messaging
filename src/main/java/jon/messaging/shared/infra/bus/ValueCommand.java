package jon.messaging.shared.infra.bus;

// Wrapper para cualquier valor que quieras usar como comando
public class ValueCommand<T> implements Command {
    private final T value;

    public ValueCommand(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}


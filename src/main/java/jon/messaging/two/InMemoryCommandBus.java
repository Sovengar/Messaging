package jon.messaging.two;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class InMemoryCommandBus implements CommandBus {
    private final Map<Class<?>, CommandHandler<?, ?>> handlers = new HashMap<>();

    public InMemoryCommandBus(List<CommandHandler<?, ?>> handlersList) {
        for (CommandHandler<?, ?> handler : handlersList) {
            Class<?> commandType = resolveCommandType(handler.getClass());
            handlers.put(commandType, handler);
        }
    }

    private Class<?> resolveCommandType(Class<?> handlerClass) {
        // Si es un proxy de CGLIB, obtener la clase real
        var realClass = handlerClass.getName().contains("$$SpringCGLIB$$")
                ? handlerClass.getSuperclass()
                : handlerClass;

        return Arrays.stream(realClass.getGenericInterfaces())
                .filter(t -> t instanceof java.lang.reflect.ParameterizedType)
                .filter(t -> t.getTypeName().contains("CommandHandler"))
                .map(t -> {
                    try {
                        java.lang.reflect.ParameterizedType paramType = (java.lang.reflect.ParameterizedType) t;
                        if (paramType.getActualTypeArguments().length > 0) {
                            return paramType.getActualTypeArguments()[0];
                        }
                        return null;
                    } catch (ClassCastException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(t -> {
                    if (t instanceof Class) {
                        return (Class<?>) t;
                    } else if (t instanceof java.lang.reflect.ParameterizedType) {
                        // Manejo de casos donde el tipo es otro tipo parametrizado
                        return (Class<?>) ((java.lang.reflect.ParameterizedType) t).getRawType();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se pudo resolver el tipo de comando para " + realClass.getName() +
                                ". Asegúrese de que implementa correctamente CommandHandler<C,R>"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R dispatch(Command command) {
        CommandHandler<Command, R> handler =
                (CommandHandler<Command, R>) handlers.get(command.getClass());

        if (handler == null)
            throw new IllegalStateException("No handler for command " + command.getClass());

        return handler.handle(command);
    }
}
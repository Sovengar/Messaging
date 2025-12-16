package jon.messaging.bus;

import lombok.RequiredArgsConstructor;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RequestBus {
    private final ApplicationContext context;

    //Podemos asegurar que el nombre del método sea "handle" haciendo que implemente la interfaz CommandHandler
    //Es el método más simple, pero cada vez que se ejecute va a cargar un mapa con todos los beans, escala poco.

    public <C, R> R dispatch(C command) {
        var handler = findHandlerForCommand(command);
        var handleMethod = findHandleMethod(handler); //Method has to be named "handle"

        if(handleMethod.getParameterCount() != 1){
            throw new IllegalArgumentException("Handler method must have exactly one parameter");
        }

        if(!handleMethod.getParameterTypes()[0].isAssignableFrom(command.getClass())){
            throw new IllegalArgumentException("Handler method parameter must be assignable from command type");
        }

        return (R) ReflectionUtils.invokeMethod(handleMethod, handler, command);
    }

    private <C> String buildNameWithoutCommandPrefix(final C command) {
        return command.getClass().getSimpleName().replace("Command", "");
    }

    private <C> String buildNameWithHandlerPrefix(final C command) {
        return command.getClass().getSimpleName() + "Handler";
    }

    private <C> Object findHandlerForCommand(final C command) {
        //String handlerNamePrefix = buildNameWithHandler(command);
        var handlerNamePrefix = buildNameWithoutCommandPrefix(command);

        // Obtener todos los beans como candidatos
        Map<String, Object> beansOfAnyType = context.getBeansOfType(Object.class);

        // Filtrar para encontrar el bean cuya clase subyacente coincide con el prefijo
        return beansOfAnyType.values().stream()
                .filter(bean -> {
                    Class<?> targetClass = AopUtils.getTargetClass(bean);
                    return targetClass.getSimpleName().equals(handlerNamePrefix);
                })
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No handler found for " + handlerNamePrefix));
    }

    private Method findHandleMethod(Object handler) {
        return Arrays.stream(handler.getClass().getMethods())
                .filter(m -> m.getName().equals("handle"))
                .findFirst()
                .orElseThrow();
    }
}

package jon.messaging.shared.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class RequestBus {
    private final ApplicationContext context;

    public <C, R> R execute(C command) {
        String handlerName = command.getClass().getSimpleName() + "Handler";
        Object handler = context.getBean(handlerName);
        return (R) ReflectionUtils.invokeMethod(
                findHandleMethod(handler), handler, command
        );
    }

    private Method findHandleMethod(Object handler) {
        return Arrays.stream(handler.getClass().getMethods())
                .filter(m -> m.getName().equals("handle"))
                .findFirst()
                .orElseThrow();
    }
}

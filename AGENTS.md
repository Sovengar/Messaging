# Agentes y Procesos del Proyecto

Este documento describe los diferentes "agentes", procesos automáticos y mecanismos clave que operan dentro de la aplicación `messaging`.

## 1. Arquitectura con Spring Modulith

El proyecto está estructurado utilizando **Spring Modulith**, lo que nos permite desarrollar un sistema monolítico con una fuerte encapsulación modular. La comunicación entre los diferentes módulos se realiza principalmente a través de un sistema de publicación de eventos.

## 2. Agentes de Publicación de Eventos

Para garantizar que los eventos de dominio se publiquen de manera fiable, incluso si los sistemas externos (como RabbitMQ) no están disponibles temporalmente, utilizamos un patrón de "Bandeja de Salida" (Outbox) implementado por Spring Modulith.

### a. Diario de Eventos en Base de Datos (JDBC Journal)

- **Qué es**: Antes de intentar enviar un evento al bróker de mensajería (AMQP), Spring Modulith lo registra en una tabla de la base de datos (`EVENT_PUBLICATION`). Esto actúa como un diario o "bandeja de salida".
- **Activación**: Está habilitado a través de la dependencia `spring-modulith-starter-jpa` y la propiedad `spring.modulith.events.jdbc.schema.initialization.enabled=true`.
- **Funcionamiento**: El evento se marca como "no completado". Una vez que se envía con éxito al bróker, se marca como "completado". Si el bróker está caído, el evento permanece en la base de datos para un reintento posterior.

### b. Agente de Republicación Automática

- **Qué es**: Es un proceso automático que se ejecuta al iniciar la aplicación. Su única función es buscar en el diario de eventos (la tabla `EVENT_PUBLICATION`) si quedaron eventos pendientes de la ejecución anterior.
- **Activación**: Está controlado por la propiedad `spring.modulith.events.republish-outstanding-events-on-restart=true`.
- **Funcionamiento**: Si encuentra eventos marcados como "no completados", intentará publicarlos de nuevo en el bróker AMQP. Esto asegura que ningún mensaje se pierda debido a una caída del servicio o del bróker.

### c. Agentes de Intervención Manual

- **Qué son**: Spring Modulith expone dos beans en el contexto de la aplicación que permiten a un operador o administrador gestionar manualmente el estado de los eventos. Son especialmente útiles para depuración o para forzar reintentos sin necesidad de reiniciar la aplicación.
- **Beans**:
    - `IncompleteEventPublications`: Permite consultar y volver a enviar los eventos que aún no se han publicado con éxito.
    - `CompletedEventPublications`: Permite consultar los eventos que ya fueron completados.
- **Uso**: Se pueden inyectar en un componente de servicio o exponer a través de un endpoint de Actuator para operaciones manuales.

## 3. Agente de Entorno de Desarrollo (Docker Compose)

- **Qué es**: Es un mecanismo proporcionado por Spring Boot para simplificar la gestión de los servicios de los que depende la aplicación durante el desarrollo (como bases de datos o brókeres de mensajería).
- **Activación**: Se realiza a través de la dependencia `spring-boot-docker-compose`.
- **Funcionamiento**: Al iniciar la aplicación en un entorno de desarrollo, Spring Boot buscará un archivo `docker-compose.yml` en la raíz del proyecto.
    - La propiedad `spring.docker.compose.lifecycle-management=start-only` le indica a Spring Boot que inicie los contenedores definidos en el compose file, pero que no los detenga al finalizar la aplicación. Esto es útil para mantener los datos en la base de datos entre ejecuciones.

## 4. Agentes de Pruebas (Testcontainers)

- **Qué son**: Son agentes que gestionan el ciclo de vida de contenedores Docker durante la ejecución de las pruebas de integración. Esto nos permite probar nuestro código contra servicios reales (como una base de datos PostgreSQL) de forma aislada y reproducible.
- **Activación**: Se realiza mediante las dependencias `spring-boot-testcontainers` y `org.testcontainers:*`.
- **Funcionamiento**:
    - **Integración con Spring Boot**: Gracias a la dependencia `spring-boot-testcontainers`, podemos definir nuestros contenedores como beans de Spring en una clase de configuración de prueba.
    - **Conexión Automática**: Al anotar un bean de contenedor con `@ServiceConnection`, Spring Boot inicia el contenedor y configura automáticamente la aplicación (por ejemplo, el `spring.datasource.url`) para que se conecte a él, sin necesidad de gestionar puertos o propiedades manualmente.
    - **Ciclo de Vida**: Los contenedores se inician antes de que comiencen las pruebas y se destruyen una vez que han finalizado, garantizando un entorno limpio para cada ejecución.
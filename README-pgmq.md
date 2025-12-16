# Spring Boot + PGMQ Experiment

This project demonstrates how to use **Postgres 16** with the **PGMQ extension** as a message queue in a **Spring Boot 4.0** application.

## Prerequisites
- Java 21+
- Docker (for TestContainers and Docker Compose)
- Maven 3.9+ (Wrapper is not included, please ensure `mvn` is installed)

## Architecture
- **Infrastructure**: `compose.yaml` uses `quay.io/tembo/pg16-pgmq:latest` to provide Postgres + PGMQ.
- **Service**: `PgmqService` wraps the raw SQL commands (`pgmq.create`, `pgmq.send`, `pgmq.read`, `pgmq.archive`).
- **Worker**: `QueueWorker` demonstrates polling, processing, and error handling (moving to DLQ).
- **Reprocessing**: `DlqManager` shows how to read from DLQ and requeue.

## Running the Application
```bash
mvn spring-boot:run
```
Spring Boot Docker Compose support will automatically start the Postgres container defined in `compose.yaml`.

## Running Tests
This project focuses on **Social Unit Tests** (Integration Tests) using TestContainers to verify behavior against a real Postgres instance.
```bash
mvn test
```
The `PgmqIntegrationTest` will create a container, send messages, verify visibility timeouts, and ensure archiving works as expected.

## Key Concepts
- **Visibility Timeout (VT)**: Messages disappear from `pgmq.read` for the duration of VT. If not archived/deleted by then, they reappear.
- **Archive**: The "happy path" removal of a message from the queue, keeping it in history.
- **Delete**: Permanent removal (used when moving to DLQ).
- **DLQ**: Implemented as a separate queue (`my_job_queue_dlq`).

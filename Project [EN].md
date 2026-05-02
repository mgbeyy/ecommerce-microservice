# Project Architecture and Design Document (MVP)

## 1. General Architectural Approach
* **Architecture Type:** Microservices
* **Communication:** Synchronous (REST via Gateway) and Asynchronous (RabbitMQ - Inter-service)
* **Design Pattern:** Layered Architecture (Controller -> Service -> Repository)
* **Distributed Transaction:** Saga Orchestration (Custom State Machine-based)

## 2. Technology Stack
* **Language/Framework:** Java & Spring Boot
* **Database:** PostgreSQL (Independent database/schema per microservice)
* **Cache/Transient Data:** Redis (For basket management)
* **Message Broker:** RabbitMQ
* **Deployment/Containerization:** Docker & Jib
* **Infrastructure:**
    * **API Gateway:** Spring Cloud Gateway
    * **Service Discovery:** Netflix Eureka
    * **Config Management:** Spring Cloud Config Server

## 3. Microservice Definitions
* **Auth Service:** User registration/login, JWT generation. Uses PostgreSQL.
* **Catalog Service:** Product listing and management, Pagination. Uses PostgreSQL.
* **Basket Service:** Add/remove/update cart. Uses Redis (AOF/RDB enabled).
* **Order Service (Orchestrator):** Order creation and workflow management. Custom State Machine operates here. Uses PostgreSQL.
* **Payment Service:** Iyzico integration. Payment records stored in PostgreSQL.

## 4. Critical Technical Decisions

### 4.1. Security Flow
* **Authentication:** JWT validation is strictly handled at the API Gateway level.
* **Propagation:** The Gateway passes the authenticated user's ID to downstream services via the `X-User-Id` header. Microservices do not handle JWTs; they only trust this header.

### 4.2. Order Flow and State Machine
* **Workflow Management:** Managed manually via a `status` column (Enum) within the Order service.
* **Transitions:** `PENDING` -> `PAYMENT_WAITING` -> `COMPLETED` / `FAILED`.
* **Error Handling:** If payment fails, the order status is set to `CANCELLED` based on the message received via RabbitMQ. State logic must be isolated in a `StateManager` component within the service layer.

### 4.3. Database and Performance
* **Audit Columns:** To resolve circular dependencies during system initialization (the bootstrap problem), the `CreatedBy` and `UpdatedBy` audit columns are designed as **nullable**.
* **Redis Persistence:** The AOF (Append Only File) feature in Redis will be enabled to prevent data loss.

### 4.4. Observability (Logging)
* **No Centralized Logging:** Logs will be kept in local files on a per-service basis.
* **Correlation ID:** A `Trace-Id` generated at the Gateway will be appended to all HTTP and RabbitMQ messages to track requests across services.

### 4.5. Containerization and Local Development (Deployment)
* **Java Services:** No Dockerfiles will be written. Image building will be handled via the Jib plugin (`mvn jib:dockerBuild`), pushing directly to the local Docker Daemon.
* **Infrastructure and Orchestration:** A comprehensive `docker-compose.yml` file will be used during the development phase to provision infrastructure components (PostgreSQL, Redis, RabbitMQ) and integrate the Java services for local testing.

## 5. Implementation Details
* **Documentation:** Swagger/OpenAPI for each service.
* **Testing:** Unit Testing with JUnit and Mockito; Integration Testing with Testcontainers.
* **Fault Tolerance:** RabbitMQ consumers must be designed as "Idempotent," and a `Retry Limit` will be applied to messages.
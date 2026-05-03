# E-Commerce Microservices Architecture Report

## 1. High-Level Overview
The project is a Spring Boot and Spring Cloud-based microservices architecture designed for an E-commerce platform. It provides a robust foundational infrastructure including centralized configuration management, service discovery, and a secure API gateway. 

**Current Status vs. Planned MVP:**
* **Currently Implemented:** The foundational infrastructure (API Gateway, Eureka Server, Config Server), a shared module (`ecommerce-common`), and the business domains (`auth-service`, `product-service`, `basket-service`, and `payment-service`) are fully built. The system employs a Layered Architecture (Controller -> Service -> Repository), handles synchronous REST routing and security validation via the Gateway, and supports asynchronous communication via RabbitMQ. Centralized Swagger API documentation is also integrated at the Gateway.
* **Planned for MVP:** The finalized architecture will include distributed caching (Redis). A major upcoming architectural feature is the Saga Orchestration pattern (via a custom State Machine) for handling distributed transactions across the Order and Payment services.

## 2. Directory Structure
The following tree represents the core source code and configuration layout.

```text
d:\Programming\n11Bootcamp\n11-bitirme\ecommerce-microservice/
├── .env                       # Global environment variables (DB credentials, JWT Secret)
├── .env.example               # Template for environment variables
├── .gitmodules                # Configuration for Git submodules
├── auth-service/              # [CURRENT] Authentication Microservice
│   ├── pom.xml
│   └── src/main/java/.../controller/AuthController.java
├── basket-service/            # [CURRENT] Basket Microservice
│   ├── pom.xml
│   └── src/main/java/.../controller/BasketController.java
├── config-server/             # [CURRENT] Spring Cloud Config Server
│   ├── pom.xml
│   └── src/main/resources/application.yaml
├── configs/                   # [CURRENT] Git submodule repository for centralized configurations
│   ├── application.yml
│   ├── auth-service.yml       # DB, Eureka, and Server configs for Auth Service
│   ├── basket-service.yml     # Redis, Eureka, and Server configs for Basket Service
│   ├── gateway-service.yml    # Routing and JWT configs for Gateway
│   ├── payment-service.yml    # DB, Eureka, and RabbitMQ configs for Payment Service
│   └── product-service.yml    # DB, Eureka, and Server configs for Product Service
├── ecommerce-common/          # [CURRENT] Shared Common Module (DTOs, events, and commands)
│   ├── pom.xml
│   └── src/main/java/com/ecommerce/common/
├── eureka-server/             # [CURRENT] Netflix Eureka Service Discovery Server
│   ├── pom.xml
│   └── src/main/resources/application.yaml
├── gateway-server/            # [CURRENT] API Gateway Service
│   ├── pom.xml
│   └── src/main/java/.../filter/AuthenticationFilter.java
├── payment-service/           # [CURRENT] Payment Microservice
│   ├── pom.xml
│   └── src/main/java/.../listener/PaymentEventListener.java
└── product-service/           # [CURRENT] Product/Catalog Microservice
    ├── pom.xml
    └── src/main/java/.../controller/ProductController.java
```

*(Note: Additional services outlined in the project blueprint, such as the Order Service, are planned but not yet implemented in this directory.)*

## 3. Microservices Breakdown

### Existing Services & Shared Modules (Phase 1 - Implemented)
* **Eureka Server (`eureka-server`):** Service Registry and Discovery. Port: `8761`.
* **Config Server (`config-server`):** Centralized configuration management using a remote Git repository. Port: `8888`.
* **API Gateway (`gateway-server`):** Single entry point for external traffic. Handles routing, strict JWT validation, and centralizes Swagger UI documentation. Port: `8080`.
* **Ecommerce Common (`ecommerce-common`):** Shared library module containing reusable DTOs, events (e.g., `PaymentFailedEvent`, `PaymentCompletedEvent`), and commands (e.g., `ProcessPaymentCommand`) used across services.
* **Authentication Service (`auth-service`):** User registration, login, and JWT generation. Uses PostgreSQL. Port: `8081`.
* **Product Service (`product-service`):** Product listing and management with pagination. Uses PostgreSQL. Port: `8082`.
* **Basket Service (`basket-service`):** Add/remove/update cart functionalities. Uses Redis (with AOF/RDB enabled for persistence). Port: `8083`.
* **Payment Service (`payment-service`):** Mocked Iyzico integration for handling payments. Uses PostgreSQL and listens for events from RabbitMQ. Port: `8084`.

### Planned Services (Phase 2 - To Be Implemented)
* **Order Service (Orchestrator):** Order creation and workflow management. Operates the custom State Machine for the Saga pattern. Will use PostgreSQL.

## 4. Configurations & Infrastructure
* **Databases & Caching:** 
  * **[CURRENT]** The `auth-service`, `product-service`, and `payment-service` use independent PostgreSQL databases. The `basket-service` uses Redis with AOF/RDB enabled for persistent basket management.
  * **[PLANNED]** Future microservices will each have independent PostgreSQL schemas.
* **Centralized Configs:** **[CURRENT]** Microservices fetch their properties from the `config-server`, which reads YAML files located in the `configs/` repository.
* **Containerization & Deployment:** **[PLANNED]** The project opts against traditional Dockerfiles. Instead, image building will be handled via the Jib plugin (`mvn jib:dockerBuild`) to push directly to the local Docker Daemon. A comprehensible `docker-compose.yml` is planned to provision the entire infrastructure (PostgreSQL, Redis, RabbitMQ) and Java services for local testing.

## 5. Inter-Service Communication & Workflow
* **Synchronous (REST):** **[CURRENT]** The API Gateway routes external requests to internal services. It validates the JWT and propagates the authenticated user's ID to downstream services via the `X-User-Id` header. Microservices trust this header and do not handle JWTs themselves.
* **Asynchronous (RabbitMQ):** **[CURRENT]** Inter-service communication relies on RabbitMQ. The `payment-service` listens for messages (`ProcessPaymentCommand`) from message queues.
* **Saga Orchestration (State Machine):** **[PLANNED]** The Order Service will manage distributed transactions through a custom `status` Enum (`PENDING` -> `PAYMENT_WAITING` -> `COMPLETED` / `FAILED`). If a payment fails, RabbitMQ messages will trigger a transition to `CANCELLED`. This state logic will be isolated in a `StateManager` component.

## 6. Additional Technical Decisions
* **Observability:** Centralized logging is intentionally omitted in favor of local file logging per service. Requests are tracked across services using a `Trace-Id`. **[CURRENT]** The Gateway successfully generates and appends this ID, and RabbitMQ events/listeners extract and propagate this `Trace-Id` to maintain traceability across async calls.
* **Database Design:** **[PLANNED]** To resolve circular dependencies during system initialization (the bootstrap problem), `CreatedBy` and `UpdatedBy` audit columns are designed as nullable.
* **Testing & Documentation:** **[CURRENT]** Swagger/OpenAPI documents each service and is aggregated at the API Gateway. **[PLANNED]** Testing will rely on JUnit/Mockito for unit tests and Testcontainers for integration tests.

## 7. Improvement Suggestions
1. **Infrastructure as Code (Docker Compose):** As outlined in the project document, implementing the `docker-compose.yml` file should be a priority to seamlessly spin up PostgreSQL, Redis, RabbitMQ, and the microservices together.
2. **Gateway Threading Model:** The Gateway uses `spring-cloud-starter-gateway-server-webmvc` instead of the traditional reactive WebFlux. Ensure Tomcat thread pools are properly sized for blocking MVC calls under load.
3. **Trace-Id Integration:** Ensure that when implementing RabbitMQ consumers/producers, the `Trace-Id` is consistently passed within message headers to maintain observability boundaries across asynchronous workflows.

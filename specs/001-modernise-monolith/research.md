# Research: Modernise Monolith Billing Platform

**Phase 0 Output** | **Date**: 2026-02-23 | **Plan**: [plan.md](plan.md)

This document resolves all NEEDS CLARIFICATION items from the Technical Context and records technology decisions with rationale.

---

## Decision 1: Application Framework — Spring Boot 3.x

**Decision**: Migrate from Jakarta EE on Open Liberty to Spring Boot 3.x.

**Rationale**:
- Azure Container Apps provides built-in managed Java components for Spring (Eureka Server, Config Server, Gateway for Spring, Admin for Spring) — these are free with the ACA environment and eliminate the need to manage infrastructure services.
- Spring Cloud Azure provides first-class integration with Azure services (Key Vault, Managed Identity, App Configuration, Service Bus) via starter dependencies.
- Spring Boot's opinionated defaults and embedded server model align perfectly with container deployment — no external application server needed.
- Spring Data JPA simplifies the migration from raw JDBC DAOs to repository interfaces with pagination, sorting, and query derivation.
- Spring Test + Testcontainers provide superior testing ergonomics for integration tests compared to Arquillian (Jakarta EE testing).
- Microsoft's official migration guidance ([Migrate Spring Boot to ACA](https://learn.microsoft.com/azure/developer/java/migration/migrate-spring-boot-to-azure-container-apps)) provides a well-documented path.
- The existing `build.gradle` already declares `jakarta.enterprise.cdi-api` and `jakarta.servlet-api` as `compileOnly` — the application is not deeply tied to the Jakarta EE programming model.

**Alternatives Considered**:
- **Stay with Jakarta EE / MicroProfile on Open Liberty**: Rejected because ACA's built-in Java components are Spring-specific (Eureka, Config Server, Gateway). Liberty would require manual service mesh configuration and has weaker Azure integration. Constitution allows "CDI or Spring-managed beans" — Spring is the better fit for Azure.
- **Quarkus**: Rejected because while it offers fast startup and GraalVM native compilation, it has weaker Azure-specific integration compared to Spring Cloud Azure. The team would need to learn a new framework with less community documentation for Azure deployments.

**Migration Approach**: Phased. Liberty remains through Phases 0–2 (test, secure, restructure). Phase 3 introduces Spring Boot alongside architecture modernisation. Phase 4 deploys decomposed Spring Boot services to ACA.

---

## Decision 2: Container Platform — Azure Container Apps

**Decision**: Deploy microservices to Azure Container Apps (ACA).

**Rationale**:
- ACA provides serverless container hosting with automatic scaling, built-in ingress, and managed HTTPS — simpler operational model than AKS.
- Built-in Spring Java components (Eureka, Gateway, Config Server) eliminate boilerplate infrastructure — these are managed by the platform.
- ACA supports blue-green deployments via revision labels and traffic weight splitting, satisfying the constitution's deployment strategy requirements.
- ACA supports Dapr for service-to-service communication, satisfying the constitution's service mesh requirements.
- Microsoft recommends ACA for "simple, isolated, and containerized Spring Cloud applications that don't require orchestration" ([Replatform Java Apps](https://learn.microsoft.com/azure/app-modernization-guidance/foundation/replatform-java-applications-onto-azure)).
- The spec's scale assumptions ("small user base") make AKS's operational complexity unnecessary.
- ACA supports Java versions 8, 11, 17, and 21 for code-based deployments and any version for container-based deployments.

**Alternatives Considered**:
- **Azure Kubernetes Service (AKS)**: Rejected for initial deployment. The small scale and four-service architecture don't justify Kubernetes complexity. ACA can be revisited if scale requirements change significantly. Constitution allows either ACA or AKS.
- **Azure App Service**: Rejected because it's optimised for single-app deployments (WAR/JAR) and lacks the multi-service orchestration features (Eureka, Gateway) needed for microservices.

---

## Decision 3: Database — Azure Database for PostgreSQL Flexible Server

**Decision**: Migrate from embedded Apache Derby to Azure Database for PostgreSQL Flexible Server.

**Rationale**:
- PostgreSQL Flexible Server offers excellent cost-performance ratio for the project's scale, starting at burstable SKUs.
- Strong Spring Data JPA support with the PostgreSQL dialect — no custom SQL required for pagination, sorting, or batch operations.
- Azure-managed backups, high availability, and optional geo-redundancy satisfy the constitution's operational requirements.
- Flyway has first-class PostgreSQL support for versioned schema migrations.
- ACA service bindings support PostgreSQL, enabling Managed Identity authentication (passwordless connection).
- Derby's SQL dialect is ANSI-compatible, making the migration to PostgreSQL straightforward.

**Alternatives Considered**:
- **Azure SQL Database**: Rejected because PostgreSQL is more cost-effective at this scale, has stronger OSS alignment, and the Derby → PostgreSQL SQL migration is no more complex than Derby → SQL Server. Constitution allows either.
- **Azure Cosmos DB**: Rejected because the application has a relational data model with foreign key relationships between entities. A document database would require significant redesign.

---

## Decision 4: Database Migrations — Flyway

**Decision**: Use Flyway for versioned, repeatable database schema migrations.

**Rationale**:
- Flyway's SQL-based migration scripts are easier to review in PRs than Liquibase's XML/YAML/JSON changelogs.
- Flyway integrates natively with Spring Boot via `spring-boot-starter-data-jpa` — migrations run automatically on application startup.
- The constitution requires "versioned migration scripts (Flyway or Liquibase)" — Flyway is simpler and sufficient.
- Flyway supports PostgreSQL natively with the `flyway-database-postgresql` module.
- Migrations are numbered sequentially (`V1__create_users.sql`, `V2__create_customers.sql`) which aligns with the phased migration approach.

**Alternatives Considered**:
- **Liquibase**: Rejected because its XML/YAML changelog format adds complexity without proportional benefit for this project's scale. Flyway's pure SQL approach is more transparent.

---

## Decision 5: CI/CD — GitHub Actions

**Decision**: Use GitHub Actions for continuous integration and deployment pipelines.

**Rationale**:
- Repository is hosted on GitHub (`owainow/ai-sdlc-modernisation`) — native integration with Actions.
- GitHub Actions has official Azure deployment actions (`azure/login`, `azure/container-apps-deploy-action`).
- Microsoft documentation provides GitHub Actions workflows for Spring Boot → ACA deployment.
- Free tier includes 2,000 minutes/month for public repositories.
- Supports the constitution's pipeline stages: compile → test → security scan → container build → integration test → deploy.

**Alternatives Considered**:
- **Azure DevOps Pipelines**: Rejected because the repo is on GitHub, and cross-platform integration adds unnecessary complexity. Azure Pipelines would be appropriate if the repo were in Azure Repos.

---

## Decision 6: Java Version — Java 21 (LTS)

**Decision**: Target Java 21 instead of Java 17.

**Rationale**:
- Java 21 is the current LTS release, supported by Azure Container Apps for both code-based and container-based deployments.
- Provides virtual threads (Project Loom) which significantly improve throughput for I/O-bound microservices without reactive programming complexity.
- Records, sealed classes, pattern matching for switch, and sequenced collections are all stable features that improve code quality.
- Microsoft provides `mcr.microsoft.com/openjdk/jdk:21` base images for containers (constitution requirement).
- Spring Boot 3.x fully supports Java 21 and leverages virtual threads with `spring.threads.virtual.enabled=true`.
- Java 17 is already 4+ years old; targeting 21 provides a longer support runway.

**Alternatives Considered**:
- **Java 17**: Rejected as the primary target. While the spec says "17+", Java 21 is the current LTS and better aligns with the constitution's "Modern Java" principle. Java 17 is the minimum floor, not the target.

---

## Decision 7: Frontend Strategy — Progressive Migration

**Decision**: Keep JSPs through Phases 0–2, extract REST APIs in Phase 2–3, introduce modern SPA in Phase 4.

**Rationale**:
- The strangler fig pattern requires maintaining the existing frontend while building new backend services.
- Extracting REST APIs from JSP logic (Phase 2) creates the contract surface that a modern frontend will consume.
- Deferring frontend framework selection to Phase 4 allows the team to focus on backend modernisation first.
- The JSP → REST API extraction naturally follows the "separate business logic from presentation" user story (P2).

**Alternatives Considered**:
- **Immediate SPA migration**: Rejected because it would create two simultaneous migration streams (frontend + backend), increasing risk and complexity. Constitution says each phase should deliver incremental value.

---

## Decision 8: Inter-Service Communication — REST + Events via Dapr

**Decision**: Use synchronous REST for queries and asynchronous events via Dapr for state-changing operations between services.

**Rationale**:
- Dapr is a built-in sidecar on Azure Container Apps — no additional infrastructure required.
- Dapr provides service invocation (REST-to-REST with retries, circuit breakers), pub/sub (events), and state management.
- Dapr satisfies the constitution's requirements for "retry logic with exponential backoff, circuit breakers, and timeout policies."
- The Reporting service (CQRS) subscribes to billing events rather than polling, enabling near-real-time read model updates.
- Dapr abstracts the underlying message broker — can start with Azure Service Bus and switch without code changes.

**Alternatives Considered**:
- **Direct REST calls with Resilience4j**: Rejected because Dapr provides the same resilience patterns as a sidecar without coupling to the application code. Constitution mentions Dapr as an option alongside Envoy.
- **Spring Cloud Stream**: Rejected because Dapr provides a broker-agnostic pub/sub API that's managed by the ACA platform, reducing operational burden.

---

## Decision 9: Observability Stack

**Decision**: OpenTelemetry SDK → Azure Monitor Application Insights, Micrometer → Azure Monitor Metrics, SLF4J + Logback → Azure Monitor Logs.

**Rationale**:
- Constitution requires OpenTelemetry for distributed tracing exported to Application Insights.
- ACA has built-in Java metrics collection for JVM-level monitoring.
- Spring Boot Actuator + Micrometer provide health checks, metrics, and readiness/liveness probes out of the box.
- Application Insights Java agent can be attached as a `-javaagent` JVM option without code changes for automatic instrumentation.
- SLF4J with Logback is the constitution-mandated logging framework.

---

## Decision 10: Secret Management — Azure Key Vault

**Decision**: All secrets stored in Azure Key Vault, referenced via Spring Cloud Azure Key Vault integration.

**Rationale**:
- Constitution requires: "All secrets, connection strings, and certificates MUST be stored in Azure Key Vault."
- Spring Cloud Azure provides `spring-cloud-azure-starter-keyvault` for automatic secret injection as Spring properties.
- ACA supports Managed Identity authentication to Key Vault — no credentials in configuration.
- Eliminates the 3 hardcoded credential locations identified in the codebase analysis (ConnectionManager, server.xml, reports.jsp).

---

## Decision 11: Caching — Azure Cache for Redis

**Decision**: Use Azure Cache for Redis for distributed caching of read-heavy data.

**Rationale**:
- Constitution requires: "Read-heavy data (billing categories, user profiles) MUST implement caching with explicit TTL and invalidation policies. Use Azure Cache for Redis for distributed cache in production."
- Billing categories and user profiles change infrequently but are read on every billing entry — ideal cache candidates.
- Spring Boot's `spring-boot-starter-cache` with `spring-boot-starter-data-redis` provides annotation-based caching (`@Cacheable`, `@CacheEvict`).
- Azure Cache for Redis supports Managed Identity authentication via Spring Cloud Azure.

**Caching Policy**:
- **User profiles**: TTL 15 minutes, evict on update/delete.
- **Billing categories**: TTL 30 minutes, evict on update/delete.
- **Customer list**: TTL 10 minutes, evict on create/update/delete.
- **Report results**: TTL 5 minutes (eventually consistent CQRS data).

**Alternatives Considered**:
- **In-memory cache (Caffeine)**: Rejected for production because each service instance would have its own cache, leading to inconsistencies across scaled replicas. Caffeine is acceptable for local development/testing.

---

## Decision 12: Feature Flags — Azure App Configuration

**Decision**: Use Azure App Configuration for feature flag management and gradual rollout.

**Rationale**:
- Constitution requires: "New features MUST be deployed behind feature flags (Azure App Configuration) to enable gradual rollout and instant disable without redeployment."
- Spring Cloud Azure provides `spring-cloud-azure-starter-appconfiguration-config` for native integration.
- Feature flags enable the strangler fig pattern — legacy JSP routes and new REST API routes can coexist, controlled by flags.
- Flags can be toggled per-environment (dev/staging/prod) without redeployment.

**Key Flags Planned**:
- `feature.rest-api.users` — enable REST API for user operations (Phase 2).
- `feature.rest-api.billing` — enable REST API for billing operations (Phase 2).
- `feature.spring-boot` — enable Spring Boot stack (Phase 3).
- `feature.microservices.*` — enable individual microservice routing (Phase 4).

---

## Decision 13: Load Testing — k6

**Decision**: Use k6 for load testing performance-critical paths.

**Rationale**:
- k6 scripts are written in JavaScript, making them accessible to the team.
- k6 integrates with Azure Load Testing for cloud-based load generation at scale.
- Constitution requires: "Performance-critical paths MUST have load test scripts validating throughput targets before deployment."
- k6 scripts will be stored in `tests/load/` and executed in CI as a pre-deployment gate.

**Target Scenarios**:
- API endpoint response time: 200ms p95 under 50 concurrent users.
- Report generation: 3 seconds for 12-month range across all customers.
- Billing summary: 2 seconds under normal load with 10,000 billable hours.

**Alternatives Considered**:
- **JMeter**: Rejected because k6 has a simpler scripting model (JavaScript vs XML) and better CI integration.
- **Locust**: Rejected because k6 has native Azure Load Testing integration.

---

## Decision 14: Container Resource Limits & Alerting

**Decision**: Define per-service container resource limits and Azure Monitor alert rules.

**Container Resource Limits** (Azure Container Apps):
| Service | CPU | Memory | Min Replicas | Max Replicas |
|---------|-----|--------|-------------|-------------|
| User Service | 0.5 vCPU | 1 Gi | 1 | 5 |
| Customer Service | 0.5 vCPU | 1 Gi | 1 | 5 |
| Billing Service | 1.0 vCPU | 2 Gi | 2 | 10 |
| Reporting Service | 1.0 vCPU | 2 Gi | 1 | 5 |

**Azure Monitor Alert Rules** (from constitution):
| Alert | Condition | Severity |
|-------|-----------|----------|
| Error rate spike | 5xx responses > 1% over 5 minutes | Sev 1 |
| Latency degradation | p95 latency > 500ms over 5 minutes | Sev 2 |
| CPU exhaustion | CPU utilisation > 80% for 10 minutes | Sev 2 |
| Memory exhaustion | Memory utilisation > 85% for 10 minutes | Sev 2 |
| Database connection failures | Connection errors > 5 in 5 minutes | Sev 1 |

**Runbooks**: Each deployed service will include a `docs/runbook.md` documenting common failure modes, diagnostic steps (log queries, health check endpoints), and recovery procedures. Runbooks are a Phase 4 deliverable alongside deployment.

**Rationale**:
- Constitution requires explicit CPU/memory limits, graceful degradation under pressure, and Azure Monitor alerts with specific thresholds.
- Billing Service gets higher resources as it is the core domain with the most transaction volume.
- Alert thresholds match the constitution's specified values.

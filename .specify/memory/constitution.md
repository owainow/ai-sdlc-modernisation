<!--
  Sync Impact Report
  ==================
  Version change: 0.0.0 (template) → 1.0.0
  Modified principles: N/A (initial ratification)
  Added sections:
    - I. Code Quality & Clean Architecture (NEW)
    - II. Test-First Development (NEW, NON-NEGOTIABLE)
    - III. User Experience Consistency (NEW)
    - IV. Performance & Scalability (NEW)
    - V. Azure-Native Design (NEW)
    - VI. Observability & Operational Excellence (NEW)
    - VII. Security by Default (NEW)
    - Azure & Microservices Deployment Standards (NEW)
    - Development Workflow & Quality Gates (NEW)
    - Governance (populated)
  Removed sections: None
  Templates requiring updates:
    - .specify/templates/plan-template.md ✅ compatible (Constitution Check
      section already present; principles can populate gates)
    - .specify/templates/spec-template.md ✅ compatible (requirements and
      success criteria sections align with principles)
    - .specify/templates/tasks-template.md ✅ compatible (phase structure
      supports test-first and story-driven workflows)
  Follow-up TODOs: None
-->

# Big Bad Monolith Constitution

## Core Principles

### I. Code Quality & Clean Architecture

All new and refactored code MUST adhere to clean architecture and
microservices-ready design patterns:

- **Separation of Concerns**: Presentation, business logic, and data
  access layers MUST be strictly isolated. No business logic in JSP
  files or controllers. No data access outside the persistence layer.
- **SOLID Principles**: Every class and module MUST follow Single
  Responsibility, Open/Closed, Liskov Substitution, Interface
  Segregation, and Dependency Inversion principles.
- **Domain-Driven Boundaries**: Service boundaries MUST align with
  bounded contexts (e.g., Billing, Customers, Users, Reporting).
  Each bounded context MUST be independently deployable.
- **Dependency Injection**: All dependencies MUST be injected, never
  directly instantiated. Use CDI (Jakarta Contexts and Dependency
  Injection) or Spring-managed beans.
- **Modern Java**: New code MUST use Java 17+ features including
  `java.time.*` (not Joda-Time), records where appropriate, sealed
  classes, and pattern matching. Legacy patterns MUST be migrated
  incrementally.
- **API-First Design**: All service interactions MUST be exposed via
  well-defined REST APIs using JAX-RS annotations. Internal service
  communication MUST use typed contracts.
- **Coding agents** MUST consult
  [Microsoft Learn Java on Azure guidance](https://learn.microsoft.com/en-us/azure/developer/java/)
  when making architectural decisions about Azure-hosted Java services.

### II. Test-First Development (NON-NEGOTIABLE)

Testing is mandatory and MUST precede implementation:

- **Red-Green-Refactor**: Tests MUST be written first, verified to
  fail (red), then implementation written to pass (green), then code
  refactored. This cycle is strictly enforced.
- **Characterization Tests**: Before modifying any legacy code, a
  characterization test MUST document the current behavior. This
  preserves regression safety during modernization.
- **Contract Tests**: Every service boundary and REST endpoint MUST
  have contract tests verifying request/response schemas. Contract
  changes MUST fail existing tests before updating.
- **Integration Tests**: Cross-service communication, database
  interactions, and end-to-end user workflows MUST have integration
  tests. Use Testcontainers for database and service dependencies.
- **Security Tests**: Known vulnerabilities (SQL injection, XSS, CSRF)
  MUST have tests proving the vulnerability exists before the fix, and
  proving the fix resolves it after.
- **Coverage Threshold**: New code MUST maintain a minimum of 80% line
  coverage. Critical paths (billing calculations, authentication)
  MUST have 95%+ coverage.
- **Test Framework**: JUnit 5 (`org.junit.jupiter.*`) is the mandatory
  test framework. Mockito for mocking. AssertJ for fluent assertions.

### III. User Experience Consistency

All user-facing interfaces MUST provide a consistent, predictable
experience across decomposed services:

- **Unified API Contract**: All REST APIs MUST follow a consistent
  response envelope format including `status`, `data`, and `errors`
  fields. Error responses MUST use RFC 7807 Problem Details format.
- **HTTP Status Codes**: APIs MUST use semantically correct HTTP
  status codes (200 OK, 201 Created, 400 Bad Request, 404 Not Found,
  422 Unprocessable Entity, 500 Internal Server Error).
- **Input Validation**: All user input MUST be validated at the API
  boundary using Bean Validation (Jakarta Validation) annotations.
  Validation errors MUST return structured, human-readable messages.
- **Pagination & Filtering**: All list endpoints MUST support
  pagination (`page`, `size`) and SHOULD support filtering and
  sorting via query parameters.
- **Versioning**: APIs MUST be versioned via URL path (e.g.,
  `/api/v1/customers`). Breaking changes MUST increment the major
  version and maintain backward compatibility for at least one
  prior version.
- **Consistent Date/Time**: All date/time values MUST use ISO 8601
  format in UTC. No locale-dependent formatting in API responses.
- **Accessibility**: Any web UI components MUST meet WCAG 2.1 AA
  compliance standards.

### IV. Performance & Scalability

Services MUST meet defined performance targets and be designed for
horizontal scalability:

- **Response Time**: API endpoints MUST respond within 200ms at p95
  under normal load. Batch/report endpoints MUST respond within 2s
  at p95.
- **Connection Pooling**: All database connections MUST use connection
  pooling (HikariCP or container-managed). No raw JDBC connection
  creation per request.
- **Caching Strategy**: Read-heavy data (billing categories, user
  profiles) MUST implement caching with explicit TTL and
  invalidation policies. Use Azure Cache for Redis for distributed
  cache in production.
- **Async Processing**: Long-running operations (report generation,
  bulk billing) MUST be processed asynchronously using message
  queues (Azure Service Bus) with status polling endpoints.
- **Database Optimization**: All queries MUST use parameterized
  statements. Queries returning lists MUST be paginated at the
  database level. N+1 query patterns are prohibited.
- **Resource Limits**: Each microservice MUST define CPU and memory
  limits for container deployment. Services MUST gracefully degrade
  under resource pressure rather than crash.
- **Load Testing**: Performance-critical paths MUST have load test
  scripts validating throughput targets before deployment.

### V. Azure-Native Design

All services MUST leverage Azure-managed services and follow the
Azure Well-Architected Framework:

- **Managed Identity**: All Azure service authentication MUST use
  Managed Identity. Key-based authentication is prohibited in
  production. Credentials MUST never be hardcoded.
- **Key Vault**: All secrets, connection strings, and certificates
  MUST be stored in Azure Key Vault. Application configuration
  MUST reference Key Vault secrets, not embed values.
- **Infrastructure as Code**: All Azure infrastructure MUST be
  defined in Bicep files under the `infra/` directory. No manual
  resource provisioning via the Azure Portal for production.
- **Container Deployment**: Services MUST be containerized and
  deployed to Azure Container Apps or Azure Kubernetes Service.
  Container images MUST be stored in Azure Container Registry.
- **Database Migration**: The embedded Derby database MUST be
  migrated to Azure SQL Database or Azure Database for PostgreSQL
  (Flexible Server) with managed backups and geo-redundancy.
- **Azure Developer CLI**: Use `azd` for provisioning and deployment
  workflows. Validate deployments with `azd provision --preview`
  before applying.
- **Coding agents** MUST consult
  [Azure Well-Architected Framework](https://learn.microsoft.com/en-us/azure/well-architected/)
  and
  [Azure Architecture Center](https://learn.microsoft.com/en-us/azure/architecture/)
  when designing service topology and infrastructure.

### VI. Observability & Operational Excellence

All services MUST be observable and operationally mature:

- **Structured Logging**: All log output MUST use structured JSON
  format with correlation IDs for distributed tracing. Use SLF4J
  with Logback or Log4j2. No `System.out.println` or JSP error
  output.
- **Distributed Tracing**: All inter-service calls MUST propagate
  trace context using OpenTelemetry. Traces MUST be exported to
  Azure Monitor Application Insights.
- **Health Checks**: Every service MUST expose `/health/live` and
  `/health/ready` endpoints following the MicroProfile Health
  specification. Kubernetes/Container Apps liveness and readiness
  probes MUST use these endpoints.
- **Metrics**: Services MUST expose key business and technical
  metrics (request count, error rate, latency histograms, active
  connections) via Micrometer or MicroProfile Metrics.
- **Alerting**: Production deployments MUST configure Azure Monitor
  alerts for error rate spikes (>1% 5xx), latency degradation
  (p95 > 500ms), and resource exhaustion (CPU > 80%, memory > 85%).
- **Runbooks**: Every deployed service MUST have a runbook
  documenting common failure modes, diagnostic steps, and
  recovery procedures.

### VII. Security by Default

Security MUST be built into every layer, not bolted on after:

- **Input Sanitization**: All user input MUST be sanitized before
  processing. String concatenation in SQL queries is strictly
  prohibited—use parameterized queries or JPA Criteria API.
- **Authentication & Authorization**: All API endpoints MUST require
  authentication. Use Azure Entra ID (formerly Azure AD) for
  identity. Role-based access control (RBAC) MUST be enforced at
  the service layer.
- **OWASP Top 10**: All services MUST be protected against the
  OWASP Top 10 vulnerabilities. SQL injection, XSS, CSRF, and
  insecure deserialization MUST have automated test coverage.
- **Dependency Scanning**: All dependencies MUST be scanned for
  known vulnerabilities using OWASP Dependency-Check or GitHub
  Dependabot. Critical/High CVEs MUST be remediated within 48
  hours of detection.
- **Transport Security**: All service communication MUST use TLS 1.2+.
  Internal service-to-service calls within the cluster MUST use
  mTLS where supported.
- **Least Privilege**: Service accounts and managed identities MUST
  follow the principle of least privilege. No service may have
  broader permissions than its operational requirements demand.
- **Coding agents** MUST consult
  [Microsoft Security Best Practices](https://learn.microsoft.com/en-us/security/benchmark/azure/)
  when implementing authentication, authorization, or data
  protection features.

## Azure & Microservices Deployment Standards

Standards governing how services are built, packaged, and deployed:

- **Container Standards**: Each microservice MUST have a Dockerfile
  using multi-stage builds. Base images MUST use Microsoft-supported
  Java images (`mcr.microsoft.com/openjdk/jdk`). Images MUST be
  scanned for vulnerabilities before deployment.
- **CI/CD Pipeline**: All deployments MUST go through automated
  pipelines. Pipeline stages MUST include: compile → test → security
  scan → container build → integration test → deploy to staging →
  smoke test → deploy to production.
- **Environment Parity**: Development, staging, and production
  environments MUST use identical infrastructure definitions
  (parameterized Bicep). Only configuration values (connection
  strings, feature flags) may differ.
- **Blue-Green Deployments**: Production deployments MUST use
  blue-green or canary deployment strategies to enable zero-downtime
  releases and instant rollback.
- **Service Mesh**: Inter-service communication MUST implement retry
  logic with exponential backoff, circuit breakers, and timeout
  policies. Use Dapr or Envoy sidecar proxies where supported by
  Azure Container Apps.
- **Database Migrations**: Schema changes MUST use versioned migration
  scripts (Flyway or Liquibase). Migrations MUST be backward
  compatible—no destructive changes without a multi-phase rollout.
- **Feature Flags**: New features MUST be deployed behind feature
  flags (Azure App Configuration) to enable gradual rollout and
  instant disable without redeployment.

## Development Workflow & Quality Gates

Rules governing how code moves from development to production:

- **Branch Strategy**: All work MUST be done on feature branches
  created from `main`. Branch names MUST follow the pattern
  `<issue-number>-<short-description>` (e.g., `042-extract-billing-service`).
- **Pull Request Requirements**: Every PR MUST include:
  - Passing CI pipeline (all tests green, no security findings)
  - Constitution compliance verification (principles checklist)
  - At least one approval from a reviewer
  - No unresolved review comments
- **Commit Messages**: Commits MUST follow Conventional Commits
  format: `<type>(<scope>): <description>` (e.g.,
  `feat(billing): add invoice generation endpoint`).
- **Quality Gates**: PRs MUST NOT be merged if:
  - Test coverage drops below 80% for new code
  - Any Critical/High security vulnerability is introduced
  - API contract tests fail
  - Performance regression detected (>10% latency increase)
- **Documentation**: Every new service, API endpoint, or
  configuration change MUST include updated documentation.
  README files MUST be kept current.
- **Coding Agent Guidance**: AI coding agents working on this
  project MUST reference `AGENT.md` for project-specific commands
  and patterns. Agents MUST consult Microsoft Learn documentation
  when implementing Azure-specific features, and MUST cite the
  relevant documentation URL in PR descriptions or code comments.

## Governance

This constitution is the supreme governance document for the
Big Bad Monolith modernization project. It supersedes all
conflicting practices, conventions, or informal agreements:

- **Supremacy**: If any practice conflicts with this constitution,
  the constitution prevails. Exceptions MUST be documented in
  the PR description with explicit justification.
- **Amendment Process**: Amendments require:
  1. A written proposal describing the change and rationale
  2. Review by at least two team members
  3. Updated version number following semantic versioning
  4. Migration plan for any affected in-flight work
- **Versioning Policy**: Constitution versions follow MAJOR.MINOR.PATCH:
  - MAJOR: Principle removal, redefinition, or backward-incompatible
    governance change
  - MINOR: New principle or section added, material expansion of
    existing guidance
  - PATCH: Wording clarification, typo fix, non-semantic refinement
- **Compliance Review**: Every PR MUST include a constitution
  compliance check. Reviewers MUST verify that changes align with
  the principles defined herein.
- **Periodic Review**: The constitution MUST be reviewed quarterly
  to ensure it reflects current project needs and Azure platform
  evolution.
- **Runtime Guidance**: Use `AGENT.md` for day-to-day development
  guidance, build commands, and project-specific patterns.

**Version**: 1.0.0 | **Ratified**: 2026-02-23 | **Last Amended**: 2026-02-23

# Implementation Plan: Modernise Monolith Billing Platform

**Branch**: `001-modernise-monolith` | **Date**: 2026-02-23 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-modernise-monolith/spec.md`

## Summary

The Big Bad Monolith is a Jakarta EE billing platform running on Open Liberty with an embedded Derby database, JSP presentation layer, and zero automated tests. The modernisation decomposes it into four bounded-context microservices (User Management, Customer Management, Billing & Time Tracking, Reporting) deployed as Spring Boot 3.x applications on Azure Container Apps, backed by Azure Database for PostgreSQL Flexible Server. The migration follows a five-phase strangler fig approach: test safety net → security fixes → architecture cleanup → technical modernisation with Spring Boot migration → microservices decomposition.

## Technical Context

**Language/Version**: Java 11 (current) → Java 21 (target, LTS, ACA-supported)
**Primary Dependencies (current)**: Jakarta EE (Servlets, CDI), Open Liberty 24.x, Joda-Time 2.12.5, Apache Derby 10.15.2, commons-dbcp2 (unused)
**Primary Dependencies (target)**: Spring Boot 3.x, Spring Data JPA, Spring Cloud Azure, Flyway, OpenTelemetry, JUnit 5, Mockito, AssertJ, Testcontainers
**Storage (current)**: Embedded Apache Derby (in-process, file-based)
**Storage (target)**: Azure Database for PostgreSQL Flexible Server (per-service schemas during decomposition)
**Testing (current)**: Zero tests, JUnit 5 declared in build.gradle but unused
**Testing (target)**: JUnit 5 + Mockito + AssertJ (unit), Testcontainers (integration), REST Assured (contract)
**Target Platform**: Azure Container Apps (serverless containers with built-in Spring Java components)
**Project Type**: Web service → microservices (4 bounded contexts)
**Build System**: Gradle (single module → multi-module)
**Performance Goals**: 200ms p95 API endpoints, 2s p95 batch/reports, connection pooling via HikariCP
**Constraints**: 80% test coverage minimum, 95% critical paths, zero hardcoded credentials, RFC 7807 error responses
**Scale/Scope**: Small user base (assumption from spec), 4 bounded contexts, 15 Java files → ~60+ files across services
**CI/CD**: GitHub Actions (repo hosted on GitHub)
**Infrastructure as Code**: Bicep (Azure-native, under `infra/` directory)
**Caching**: Azure Cache for Redis (TTL-based, annotation-driven via Spring Cache)
**Feature Flags**: Azure App Configuration (`spring-cloud-azure-starter-appconfiguration-config`)
**Load Testing**: k6 (scripts in `tests/load/`, integrated with Azure Load Testing)
**Container Registry**: Azure Container Registry (ACR) — Microsoft-supported Java base images
**Alerting**: Azure Monitor (>1% 5xx, p95 >500ms, CPU >80%, memory >85%)
**Runbooks**: One per deployed service in `docs/runbook.md` (Phase 4 deliverable)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| # | Constitution Principle | Spec Alignment | Status |
|---|----------------------|----------------|--------|
| I | Code Quality & Clean Architecture | FR-007 (service interfaces), FR-009 (DI), FR-010 (java.time), FR-011 (Java 17+), User Story 3 (layer separation). Spring Boot 3.x, Java 21, layered project structure. | ✅ PASS |
| II | Test-First Development (NON-NEGOTIABLE) | User Story 1 is P1, FR-001 (tests before refactoring), SC-001 (80%/95% coverage). JUnit 5 + Mockito + AssertJ + Testcontainers + REST Assured. Characterisation tests before legacy changes. | ✅ PASS |
| III | User Experience Consistency | FR-006 (input validation), FR-012 (pagination). API contracts define: RFC 7807 error format, `/api/v1/` versioning, ISO 8601 dates, consistent response envelope `{status, data, errors}`. | ✅ PASS |
| IV | Performance & Scalability | FR-013 (HikariCP pooling), FR-014 (N+1 fix), 200ms p95 API target. Azure Cache for Redis (Decision 11), container resource limits (Decision 14), k6 load testing (Decision 13). | ✅ PASS |
| V | Azure-Native Design | Managed Identity, Key Vault (Decision 10), Bicep IaC, ACA (Decision 2), PostgreSQL Flexible Server (Decision 3), ACR for container images, App Configuration (Decision 12). | ✅ PASS |
| VI | Observability & Operational Excellence | OpenTelemetry → Application Insights (Decision 9), SLF4J + Logback, Spring Actuator health checks, Micrometer metrics. Azure Monitor alerts with constitution thresholds (Decision 14). Runbooks per service (Phase 4 deliverable). | ✅ PASS |
| VII | Security by Default | User Story 2 is P1, FR-002 (XSS), FR-003 (CSRF), FR-004 (credential externalisation). Azure Entra ID + RBAC scopes on all contracts. Key Vault. GitHub Dependabot for dependency scanning. | ✅ PASS |
| — | Deployment Standards | Multi-stage Dockerfiles with MCR Java images. GitHub Actions CI/CD (Decision 5). Blue-green via ACA revision labels. Flyway (Decision 4). Dapr service mesh (Decision 8). Azure App Configuration feature flags (Decision 12). | ✅ PASS |
| — | Quality Gates | Branch naming `<issue>-<description>`, conventional commits, PR requirements, 80% coverage gate, contract test gate, performance regression gate. | ✅ PASS |

**Gate Result**: ✅ ALL GATES PASS — no violations requiring justification.

## Project Structure

### Documentation (this feature)

```text
specs/001-modernise-monolith/
├── plan.md              # This file
├── research.md          # Phase 0 output — technology decisions
├── data-model.md        # Phase 1 output — entity design
├── quickstart.md        # Phase 1 output — developer setup guide
├── contracts/           # Phase 1 output — API contracts
│   ├── user-api.md
│   ├── customer-api.md
│   ├── billing-api.md
│   └── reporting-api.md
└── tasks.md             # Phase 2 output (/speckit.tasks — NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
# Current monolith structure (Phases 0–3)
src/
├── main/
│   ├── java/com/sourcegraph/demo/bigbadmonolith/
│   │   ├── entity/          # User, Customer, BillingCategory, BillableHour
│   │   ├── dao/             # DAO classes → evolve to Repository interfaces
│   │   ├── service/         # Business logic services
│   │   ├── controller/      # NEW: JAX-RS/REST controllers (Phase 2)
│   │   ├── dto/             # NEW: Request/Response DTOs (Phase 2)
│   │   ├── config/          # NEW: Application configuration (Phase 2)
│   │   ├── exception/       # NEW: Exception handling (Phase 2)
│   │   └── util/            # DateTimeUtils → migrated to java.time
│   ├── liberty/config/      # server.xml, bootstrap.properties
│   ├── resources/
│   │   ├── META-INF/beans.xml
│   │   └── db/migration/    # NEW: Flyway migration scripts (Phase 2)
│   └── webapp/              # JSPs (legacy, replaced in Phase 4)
└── test/
    └── java/com/sourcegraph/demo/bigbadmonolith/
        ├── entity/          # NEW: Entity unit tests (Phase 0)
        ├── dao/             # NEW: DAO characterisation tests (Phase 0)
        ├── service/         # NEW: Service unit tests (Phase 0)
        ├── controller/      # NEW: Controller integration tests (Phase 2)
        └── integration/     # NEW: End-to-end tests (Phase 0)

# Target multi-module structure (Phase 4 — decomposition)
services/
├── user-service/
│   ├── src/main/java/.../user/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   ├── src/main/resources/
│   └── src/test/
├── customer-service/
│   └── [same structure]
├── billing-service/
│   └── [same structure]
├── reporting-service/
│   └── [same structure]
└── shared/
    ├── common-dto/          # Shared DTOs for inter-service contracts
    └── common-test/         # Shared test utilities

infra/                       # Azure Bicep IaC
├── main.bicep
├── modules/
│   ├── container-apps.bicep
│   ├── postgresql.bicep
│   ├── keyvault.bicep
│   └── monitoring.bicep
└── parameters/
    ├── dev.bicepparam
    ├── staging.bicepparam
    └── prod.bicepparam
```

**Structure Decision**: Single-module monolith maintained through Phases 0–3 (test, secure, restructure, modernise). Phase 4 introduces Gradle multi-module project under `services/` with shared modules. Infrastructure-as-code lives in `infra/` from Phase 4 onward. This follows the strangler fig pattern — new services are added alongside the monolith, with traffic gradually redirected.

## Complexity Tracking

> No Constitution Check violations requiring justification. All gates passed.

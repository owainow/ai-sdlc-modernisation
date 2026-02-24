# Big Bad Monolith — Modernised to Microservices

A Jakarta EE billing platform modernised from a JSP monolith to **4 Spring Boot 3.x microservices** deployed on Azure Container Apps. This repository tracks the complete strangler fig migration journey.

## Modernisation Status

| Phase | Status | Description |
|-------|--------|-------------|
| Phase 1-3 | ✅ Complete | Test infrastructure, CI pipeline, 85%+ coverage safety net |
| Phase 4 (US2) | ✅ Complete | Security fixes — XSS escaping, credential externalisation, error pages |
| Phase 5 (US3) | ✅ Complete | Architecture — Service interfaces, exception hierarchy, Flyway |
| Phase 6 (US4) | ✅ Complete | Date/Time — Joda-Time → java.time, Java 17+ target |
| Phase 7 (US5) | ✅ Complete | Performance — HikariCP pooling, pagination, N+1 elimination |
| Phase 8 (US6) | ✅ Complete | Decompose into 4 Spring Boot microservices on Azure Container Apps |
| Phase 9 | ✅ Complete | Polish — data migration, documentation, Dependabot |

## Microservices Architecture

| Service | Port | API Base | Description |
|---------|------|----------|-------------|
| **user-service** | 8081 | `/api/v1/users` | User CRUD, BCrypt authentication |
| **customer-service** | 8082 | `/api/v1/customers` | Customer CRUD with delete-guard |
| **billing-service** | 8083 | `/api/v1/categories`, `/api/v1/hours`, `/api/v1/billing/summary` | Categories, Hours (24h daily cap), Billing Summary, Dapr events |
| **reporting-service** | 8084 | `/api/v1/reports` | Monthly, Range, Utilisation reports (CQRS read model) |

### Shared Modules
- **common-dto** — `ApiResponse` envelope, `PaginatedResponse`, RFC 7807 support
- **common-test** — `TestDataFactory`, shared test utilities

## Quick Start

```bash
# Clone
git clone https://github.com/owainow/ai-sdlc-modernisation.git
cd ai-sdlc-modernisation

# Build all services
./gradlew clean build

# Run all tests (monolith + microservices)
./gradlew test

# Start all microservices locally
docker compose up --build

# Or run individually
./gradlew :services:user-service:bootRun
./gradlew :services:customer-service:bootRun
./gradlew :services:billing-service:bootRun
./gradlew :services:reporting-service:bootRun
```

See [`specs/001-modernise-monolith/quickstart.md`](specs/001-modernise-monolith/quickstart.md) for the full 30-minute setup guide.

## Domain Model

### Core Entities (UUID primary keys, `java.time.*`)
- **User**: Employees who log billable hours (BCrypt passwords, Bean Validation)
- **Customer**: Companies billed for services (unique name constraint)
- **BillingCategory**: Work categories with hourly rates (0 < rate ≤ 10,000)
- **BillableHour**: Time entries (> 0, ≤ 24h daily cap per user, `java.time.LocalDate`)

### Database (PostgreSQL per service)
| Database | Service | Tables |
|----------|---------|--------|
| `userdb` | user-service | `users` |
| `customerdb` | customer-service | `customers` |
| `billingdb` | billing-service | `billing_categories`, `billable_hours` |
| `reportingdb` | reporting-service | `billing_read_model` (CQRS projection) |

## Build & Test

```bash
./gradlew compileJava                    # Compile all modules
./gradlew test                           # Run all tests
./gradlew jacocoTestReport               # Generate coverage report
./gradlew jacocoTestCoverageVerification # Verify 80% coverage gate

# Individual service tests
./gradlew :services:user-service:test
./gradlew :services:customer-service:test
./gradlew :services:billing-service:test
./gradlew :services:reporting-service:test
```

## API Contracts

All services follow the `{status, data, errors}` response envelope with RFC 7807 ProblemDetail for errors.

- [User API](specs/001-modernise-monolith/contracts/user-api.md) — CRUD, pagination, 409 on duplicate username
- [Customer API](specs/001-modernise-monolith/contracts/customer-api.md) — CRUD, 409 on delete with linked hours
- [Billing API](specs/001-modernise-monolith/contracts/billing-api.md) — Categories + Hours + Summary, Dapr events
- [Reporting API](specs/001-modernise-monolith/contracts/reporting-api.md) — Monthly, Range, Utilisation (CQRS)

## Project Structure

```
├── services/                           # Spring Boot microservices
│   ├── user-service/                   # User Management
│   ├── customer-service/               # Customer Management
│   ├── billing-service/                # Billing & Time Tracking
│   ├── reporting-service/              # Reporting (CQRS)
│   └── shared/
│       ├── common-dto/                 # ApiResponse, PaginatedResponse
│       └── common-test/               # TestDataFactory
├── src/                                # Legacy monolith (kept for reference)
│   ├── main/java/                      # Modernised monolith code
│   └── test/java/                      # 85%+ coverage safety net
├── infra/                              # Azure Bicep IaC
│   ├── main.bicep                      # Orchestrator
│   ├── modules/                        # ACA, PostgreSQL, Key Vault, Redis, monitoring
│   └── parameters/                     # dev, staging, prod environments
├── tests/load/                         # k6 load test scripts
├── docs/runbooks/                      # Per-service operational runbooks
├── scripts/                            # Data migration scripts
├── .github/workflows/
│   ├── ci.yml                          # CI: compile → test → coverage
│   └── deploy.yml                      # CD: build → test → ACR → ACA
├── docker-compose.yml                  # Local dev (4 services + PostgreSQL + Redis)
├── build.gradle                        # Root build with Spring Boot BOM
└── settings.gradle                     # Multi-module configuration
```

## Infrastructure (Azure)

Deployed via Bicep IaC under `infra/`:

| Resource | Module | Description |
|----------|--------|-------------|
| Azure Container Apps | `container-apps.bicep` | 4 services with auto-scaling |
| PostgreSQL Flexible Server | `postgresql.bicep` | 4 databases, zone-redundant (prod) |
| Azure Key Vault | `keyvault.bicep` | Secrets with Managed Identity |
| Azure Cache for Redis | `redis.bicep` | Caching with TLS 1.2+ |
| Azure Monitor + App Insights | `monitoring.bicep` | Metrics, alerts, distributed tracing |

```bash
# Deploy to Azure
az deployment sub create --location uksouth \
  --template-file infra/main.bicep \
  --parameters infra/parameters/dev.bicepparam
```

## Legacy Monolith (Open Liberty)

The original monolith is preserved under `src/` with its test safety net:

```bash
./liberty-dev.sh                        # Start Liberty in dev mode
# Access: http://localhost:9080/big-bad-monolith/
```

## Configuration

### Microservices (Environment Variables)
| Variable | Description |
|----------|-------------|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |

### Legacy Monolith (Environment Variables)
| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:derby:./data/bigbadmonolith;create=true` | Database JDBC URL |
| `DB_USER` | `app` | Database username |
| `DB_PASSWORD` | `app` | Database password |

## Documentation

- **Spec**: [`specs/001-modernise-monolith/spec.md`](specs/001-modernise-monolith/spec.md)
- **Plan**: [`specs/001-modernise-monolith/plan.md`](specs/001-modernise-monolith/plan.md)
- **Tasks**: [`specs/001-modernise-monolith/tasks.md`](specs/001-modernise-monolith/tasks.md)
- **Data Model**: [`specs/001-modernise-monolith/data-model.md`](specs/001-modernise-monolith/data-model.md)
- **API Contracts**: [`specs/001-modernise-monolith/contracts/`](specs/001-modernise-monolith/contracts/)
- **Quickstart**: [`specs/001-modernise-monolith/quickstart.md`](specs/001-modernise-monolith/quickstart.md)
- **Constitution**: [`.specify/memory/constitution.md`](.specify/memory/constitution.md)
- **Runbooks**: [`docs/runbooks/`](docs/runbooks/)

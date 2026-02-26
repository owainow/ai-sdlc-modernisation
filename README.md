# Big Bad Monolith — Greenfield Rebuild

A time-tracking billing platform rebuilt from a Jakarta EE/Derby monolith into **4 Spring Boot 3.4.1 microservices** with a **React/TypeScript SPA**, targeting Azure Container Apps. All [28 legacy features](specs/002-greenfield-rebuild/spec.md) are preserved with functional parity.

## Architecture

```
┌──────────────┐
│  React SPA   │──── Vite dev :5173  ──── /api proxy ────┐
│  (TypeScript) │                                         │
└──────────────┘                                          ▼
          ┌──────────┬──────────┬───────────┬────────────┐
          │  User    │ Customer │ Billing   │ Reporting  │
          │  :8081   │  :8082   │  :8083    │  :8084     │
          └────┬─────┴────┬─────┴────┬──────┴────┬───────┘
               │          │          │           │
          ┌────┴──────────┴──────────┴───────────┴────┐
          │  PostgreSQL 16 (schema-per-service)        │
          │  user_svc │ customer_svc │ billing_svc │   │
          │           │              │ reporting_svc│   │
          └────────────────────────────────────────────┘
```

| Service | Port | API Base | Responsibilities |
|---------|------|----------|-----------------|
| **user-service** | 8081 | `/api/v1/users` | User CRUD, email uniqueness |
| **customer-service** | 8082 | `/api/v1/customers` | Customer CRUD, name uniqueness, search |
| **billing-service** | 8083 | `/api/v1/billing/categories`, `/api/v1/billing/hours` | Categories, hours, 24h validation, rate snapshots, revenue dashboard |
| **reporting-service** | 8084 | `/api/v1/reports` | Customer bill, monthly summary, revenue summary (CQRS) |
| **migration-tool** | — | CLI | Derby → PostgreSQL data migration with validation |

---

## Quick Start — Run Locally

### Prerequisites

| Tool | Version | Install |
|------|---------|---------|
| Java JDK | **21** (LTS) | [Eclipse Temurin](https://adoptium.net/) |
| Node.js | **20+** (LTS) | [nodejs.org](https://nodejs.org/) |
| Docker Desktop | 4.x+ | [docker.com](https://www.docker.com/products/docker-desktop/) |
| Git | 2.x+ | [git-scm.com](https://git-scm.com/) |

### Option A: Docker Compose (all services — recommended)

Builds and runs all 4 microservices + PostgreSQL + Redis in containers:

```bash
# Start everything (first run builds images — ~3 min)
docker compose up --build

# Run in background
docker compose up --build -d

# View logs
docker compose logs -f

# Stop everything
docker compose down

# Stop and remove data volumes
docker compose down -v
```

**Access points once running:**

| Component | URL |
|-----------|-----|
| User Service | http://localhost:8081/api/v1/users |
| Customer Service | http://localhost:8082/api/v1/customers |
| Billing Service | http://localhost:8083/api/v1/billing/categories |
| Reporting Service | http://localhost:8084/api/v1/reports/revenue-summary |
| Actuator (any service) | http://localhost:808X/actuator/health |

### Option B: Run services individually (for development)

**Step 1 — Start infrastructure:**

```bash
docker compose -f docker-compose.dev.yml up -d
```

This starts PostgreSQL 16 (with 4 schemas auto-created) and Redis 7.

**Step 2 — Run microservices with PostgreSQL:**

Each service needs environment variables pointing to PostgreSQL. Run from the repo root:

```bash
# Terminal 1 — User Service
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres?currentSchema=user_svc \
SPRING_DATASOURCE_USERNAME=postgres \
SPRING_DATASOURCE_PASSWORD=postgres \
SPRING_DATASOURCE_DRIVER=org.postgresql.Driver \
DB_SCHEMA=user_svc \
./gradlew :services:user-service:bootRun

# Terminal 2 — Customer Service
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres?currentSchema=customer_svc \
SPRING_DATASOURCE_USERNAME=postgres \
SPRING_DATASOURCE_PASSWORD=postgres \
SPRING_DATASOURCE_DRIVER=org.postgresql.Driver \
DB_SCHEMA=customer_svc \
./gradlew :services:customer-service:bootRun

# Terminal 3 — Billing Service
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres?currentSchema=billing_svc \
SPRING_DATASOURCE_USERNAME=postgres \
SPRING_DATASOURCE_PASSWORD=postgres \
SPRING_DATASOURCE_DRIVER=org.postgresql.Driver \
DB_SCHEMA=billing_svc \
REDIS_HOST=localhost \
CACHE_TYPE=redis \
./gradlew :services:billing-service:bootRun

# Terminal 4 — Reporting Service
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres?currentSchema=reporting_svc \
SPRING_DATASOURCE_USERNAME=postgres \
SPRING_DATASOURCE_PASSWORD=postgres \
SPRING_DATASOURCE_DRIVER=org.postgresql.Driver \
DB_SCHEMA=reporting_svc \
./gradlew :services:reporting-service:bootRun
```

**Step 3 — Run frontend:**

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at http://localhost:5173 and proxies `/api` requests to `localhost:8080` (the future API Gateway port). For individual service development without a gateway, update the `target` in `vite.config.ts` to the specific service port (e.g., `http://localhost:8081` for user-service).

### Option C: Run with H2 (no Docker required — quick testing)

Services default to in-memory H2 when no PostgreSQL env vars are set:

```bash
# Just run a service — uses H2 in-memory database
./gradlew :services:user-service:bootRun
./gradlew :services:customer-service:bootRun
./gradlew :services:billing-service:bootRun
./gradlew :services:reporting-service:bootRun
```

> **Note**: H2 mode is useful for quick API testing but data is lost on restart. Cross-service features (reporting) won't work across separate H2 instances.

---

## Build & Test

```bash
# Build all services (skipping tests)
./gradlew build -x test

# Run all tests (65 tests across 5 modules)
./gradlew test

# Run tests for a specific service
./gradlew :services:billing-service:test

# Run a specific test class
./gradlew :services:billing-service:test --tests "*BillableHourServiceTest"

# Build a single service JAR
./gradlew :services:user-service:bootJar
```

### Frontend

```bash
cd frontend
npm install          # Install dependencies
npm run dev          # Dev server at :5173
npm run build        # Production build
npm run lint         # ESLint
```

---

## Derby → PostgreSQL Migration Tool

The migration tool at `services/migration-tool/` migrates data from the legacy Apache Derby database to the new PostgreSQL microservice schemas.

### What it does

1. Reads all 4 Derby tables (users, customers, billing_categories, billable_hours)
2. Maps integer IDs to deterministic UUIDs (same input always produces same UUID)
3. Backfills `rate_snapshot` on billable hours from the category's hourly rate
4. Writes to all 4 PostgreSQL schemas + reporting read models
5. Validates: row counts, revenue parity, FK integrity

### Running the migration

**Prerequisites:** PostgreSQL must be running with schemas created (use `docker compose -f docker-compose.dev.yml up -d`).

```bash
# Build the migration tool
./gradlew :services:migration-tool:bootJar

# Run with defaults (Derby at ./data/bigbadmonolith, PostgreSQL at localhost:5432)
java -jar services/migration-tool/build/libs/migration-tool-0.0.1-SNAPSHOT.jar

# Run with custom connection strings
DERBY_URL=jdbc:derby:/path/to/derby/data \
PG_USER_URL=jdbc:postgresql://myhost:5432/mydb?currentSchema=user_svc \
PG_CUSTOMER_URL=jdbc:postgresql://myhost:5432/mydb?currentSchema=customer_svc \
PG_BILLING_URL=jdbc:postgresql://myhost:5432/mydb?currentSchema=billing_svc \
PG_REPORTING_URL=jdbc:postgresql://myhost:5432/mydb?currentSchema=reporting_svc \
PG_USER=postgres \
PG_PASSWORD=postgres \
java -jar services/migration-tool/build/libs/migration-tool-0.0.1-SNAPSHOT.jar
```

### Migration environment variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DERBY_URL` | `jdbc:derby:./data/bigbadmonolith;create=false` | Source Derby JDBC URL |
| `DERBY_USER` | `app` | Derby username |
| `DERBY_PASSWORD` | `app` | Derby password |
| `PG_USER_URL` | `jdbc:postgresql://localhost:5432/postgres?currentSchema=user_svc` | Target: user-service schema |
| `PG_CUSTOMER_URL` | `jdbc:postgresql://localhost:5432/postgres?currentSchema=customer_svc` | Target: customer-service schema |
| `PG_BILLING_URL` | `jdbc:postgresql://localhost:5432/postgres?currentSchema=billing_svc` | Target: billing-service schema |
| `PG_REPORTING_URL` | `jdbc:postgresql://localhost:5432/postgres?currentSchema=reporting_svc` | Target: reporting-service schema |
| `PG_USER` | `postgres` | PostgreSQL username |
| `PG_PASSWORD` | `postgres` | PostgreSQL password |

### Running migration tests

```bash
./gradlew :services:migration-tool:test
```

7 integration tests verify: data transfer, deterministic UUIDs, idempotency, rate snapshot backfill, reporting read models, revenue parity, and full validation.

---

## Service Environment Variables

All services share this configuration pattern:

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:h2:mem:testdb` | JDBC URL (H2 for dev, PostgreSQL for production) |
| `SPRING_DATASOURCE_USERNAME` | `sa` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | *(empty)* | Database password |
| `SPRING_DATASOURCE_DRIVER` | `org.h2.Driver` | JDBC driver class |
| `DB_SCHEMA` | `public` | Database schema name |
| `SERVER_PORT` | Service-specific | HTTP port (8081-8084) |
| `REDIS_HOST` | `localhost` | Redis host (billing-service only) |
| `REDIS_PORT` | `6379` | Redis port (billing-service only) |
| `CACHE_TYPE` | `none` | Cache type — set to `redis` to enable (billing-service only) |

---

## Project Structure

```
/
├── services/
│   ├── user-service/              # Spring Boot — User CRUD (port 8081)
│   ├── customer-service/          # Spring Boot — Customer CRUD (port 8082)
│   ├── billing-service/           # Spring Boot — Categories + Hours (port 8083)
│   ├── reporting-service/         # Spring Boot — CQRS Reports (port 8084)
│   └── migration-tool/            # Derby → PostgreSQL migration CLI
├── frontend/                      # React/TypeScript SPA (Vite)
├── libs/
│   ├── common-dto/                # Shared ApiResponse<T>, ProblemDetail, GlobalExceptionHandler
│   └── test-utils/                # Shared test utilities
├── infra/                         # Azure Bicep IaC
├── scripts/
│   └── init-schemas.sql           # PostgreSQL schema initialisation
├── src/                           # Legacy monolith (Jakarta EE + Derby)
├── docker-compose.yml             # Full stack: all services + infra
├── docker-compose.dev.yml         # Infrastructure only: PostgreSQL + Redis
├── build.gradle                   # Root Gradle build (Java 21 toolchain)
└── settings.gradle                # Multi-module configuration
```

---

## Legacy Application

The original monolith at `src/` (Java 11, Jakarta EE, JSP, Apache Derby) is preserved for reference and migration source data.

```bash
# Run legacy monolith (requires Open Liberty)
./liberty-dev.sh        # Linux/macOS
liberty-dev.bat         # Windows

# Legacy URL: http://localhost:9080/big-bad-monolith/
```

## Specs & Documentation

- **Greenfield spec**: `specs/002-greenfield-rebuild/spec.md`
- **Plan**: `specs/002-greenfield-rebuild/plan.md`
- **Data model**: `specs/002-greenfield-rebuild/data-model.md`
- **API contracts**: `specs/002-greenfield-rebuild/contracts/`
- **Constitution**: `.specify/memory/constitution.md`
- **Legacy spec**: `specs/001-modernise-monolith/spec.md`

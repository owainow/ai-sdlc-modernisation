# Big Bad Monolith → Microservices on Azure Container Apps

A Jakarta EE billing platform modernised from a JSP monolith to **4 Spring Boot 3.x microservices** deployed on **Azure Container Apps** with full CI/CD, infrastructure-as-code, and observability.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Azure Container Apps                      │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  │ user-service  │  │  customer-   │  │  billing-    │  │  reporting-  │
│  │   :8080       │  │  service     │  │  service     │  │  service     │
│  │              │  │   :8080      │  │   :8080      │  │   :8080      │
│  │ /api/v1/     │  │ /api/v1/     │  │ /api/v1/     │  │ /api/v1/     │
│  │   users      │  │   customers  │  │   categories │  │   reports    │
│  └──────┬───────┘  └──────┬───────┘  │   hours      │  │  (CQRS)     │
│         │                 │          │   billing/    │  └──────┬───────┘
│         │                 │          │   summary     │         │
│         │                 │          └───────┬───────┘         │
│         │                 │                  │                 │
│         │                 │          Dapr pub/sub ─────────────┘
│  ┌──────▼───┐      ┌─────▼────┐    ┌───────▼──┐      ┌───────▼──┐
│  │  userdb  │      │customerdb│    │ billingdb│      │reportingdb│
│  └──────────┘      └──────────┘    └──────────┘      └──────────┘
│                    PostgreSQL Flexible Server                     │
├──────────────────────────────────────────────────────────────────┤
│  Key Vault │ Redis Cache │ App Insights │ Azure Monitor          │
└──────────────────────────────────────────────────────────────────┘
```

| Service | Host Port | API Base | Description |
|---------|-----------|----------|-------------|
| **user-service** | 8081 | `/api/v1/users` | User CRUD, BCrypt authentication |
| **customer-service** | 8082 | `/api/v1/customers` | Customer CRUD with delete-guard |
| **billing-service** | 8083 | `/api/v1/categories`, `/api/v1/hours`, `/api/v1/billing/summary` | Categories, Hours (24h cap), Billing Summary, Dapr events |
| **reporting-service** | 8084 | `/api/v1/reports` | Monthly, Range, Utilisation reports (CQRS read model) |

---

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| **Java (JDK)** | 17+ | Build and run services |
| **Docker** + **Docker Compose** | Latest | Local development with containers |
| **Azure CLI** | 2.50+ | Azure deployment (optional for local dev) |
| **Gradle** | Wrapper included | Build automation (`./gradlew`) |

---

## Running Locally

### Option 1: Docker Compose (Recommended)

Starts all 4 microservices with PostgreSQL and Redis — **no local Java or database setup needed**.

```bash
# 1. Clone the repository
git clone https://github.com/owainow/ai-sdlc-modernisation.git
cd ai-sdlc-modernisation

# 2. Build and start all services
docker compose up --build

# 3. Wait for healthy status (~30 seconds), then verify
curl http://localhost:8081/actuator/health   # user-service
curl http://localhost:8082/actuator/health   # customer-service
curl http://localhost:8083/actuator/health   # billing-service
curl http://localhost:8084/actuator/health   # reporting-service
```

**What Docker Compose provides:**
- PostgreSQL 16 with 4 databases (`userdb`, `customerdb`, `billingdb`, `reportingdb`)
- Redis 7 for caching
- All 4 Spring Boot services (port 8080 in-container, mapped to host 8081–8084)
- Flyway auto-runs database migrations on startup

### Try the APIs

```bash
# Create a user
curl -X POST http://localhost:8081/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username":"jsmith","password":"secret123","firstName":"John","lastName":"Smith"}'

# Create a customer
curl -X POST http://localhost:8082/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{"name":"Acme Corp","contactEmail":"billing@acme.com"}'

# Create a billing category
curl -X POST http://localhost:8083/api/v1/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Development","hourlyRate":150.00}'

# List all users (paginated)
curl http://localhost:8081/api/v1/users

# Get monthly report
curl "http://localhost:8084/api/v1/reports/monthly?year=2025&month=1"
```

### Option 2: Run Services Individually (Development)

Requires a local PostgreSQL instance with databases created.

```bash
# Build everything
./gradlew clean build

# Run individual services (each needs SPRING_DATASOURCE_URL configured)
./gradlew :services:user-service:bootRun
./gradlew :services:customer-service:bootRun
./gradlew :services:billing-service:bootRun
./gradlew :services:reporting-service:bootRun
```

### Stopping Services

```bash
docker compose down            # Stop services, keep data
docker compose down -v         # Stop services and delete data
```

---

## Build & Test

```bash
./gradlew compileJava                    # Compile all modules
./gradlew test                           # Run all 182 tests
./gradlew jacocoTestReport               # Generate coverage report
./gradlew jacocoTestCoverageVerification # Verify 80% coverage gate

# Individual service tests
./gradlew :services:user-service:test
./gradlew :services:customer-service:test
./gradlew :services:billing-service:test
./gradlew :services:reporting-service:test
```

**Test breakdown**: 157 monolith safety-net tests + 25 microservice contract tests = **182 total tests, 83% coverage**.

---

## Deploying to Azure

### CI/CD Pipelines

This repository includes two GitHub Actions workflows:

| Workflow | File | Trigger | Purpose |
|----------|------|---------|---------|
| **CI** | `.github/workflows/ci.yml` | Push/PR to `main` | Compile → Test → Coverage check |
| **Deploy** | `.github/workflows/deploy.yml` | Push to `main` or manual dispatch | Build → Test → Container build → ACR push → ACA deploy |

### Step 1: Provision Azure Infrastructure

The infrastructure is defined as Bicep IaC under `infra/`. Deploy it with the Azure CLI:

```bash
# Login to Azure
az login

# Deploy infrastructure (dev environment)
az deployment sub create \
  --location uksouth \
  --template-file infra/main.bicep \
  --parameters infra/parameters/dev.bicepparam \
  --parameters postgresAdminPassword='<YOUR_SECURE_PASSWORD>'
```

This creates:

| Resource | Bicep Module | Description |
|----------|-------------|-------------|
| Resource Group | `main.bicep` | `rg-billing-dev` |
| Azure Container Apps Environment | `modules/container-apps.bicep` | 4 container apps with auto-scaling |
| PostgreSQL Flexible Server | `modules/postgresql.bicep` | 4 databases (`userdb`, `customerdb`, `billingdb`, `reportingdb`) |
| Azure Key Vault | `modules/keyvault.bicep` | Secrets with Managed Identity access |
| Azure Cache for Redis | `modules/redis.bicep` | TLS 1.2+, caching layer |
| Azure Monitor + App Insights | `modules/monitoring.bicep` | Metrics, alerts, distributed tracing |

**Environment parameter files:**
- `infra/parameters/dev.bicepparam` — Development (B1 tier, 1 replica)
- `infra/parameters/staging.bicepparam` — Staging
- `infra/parameters/prod.bicepparam` — Production (zone-redundant, multi-replica)

### Step 2: Create Azure Container Registry (ACR)

```bash
# Create ACR (one-time setup)
az acr create --resource-group rg-billing-dev \
  --name acrbilling --sku Basic

# Enable admin access (for GitHub Actions)
az acr update --name acrbilling --admin-enabled true

# Get credentials
az acr credential show --name acrbilling
```

### Step 3: Configure GitHub Secrets

Add these secrets to your GitHub repository (`Settings → Secrets and variables → Actions`):

| Secret | Description | How to get it |
|--------|-------------|---------------|
| `AZURE_CLIENT_ID` | Service principal app ID | `az ad sp create-for-rbac --name gh-billing-deploy` |
| `AZURE_TENANT_ID` | Azure AD tenant ID | `az account show --query tenantId` |
| `AZURE_SUBSCRIPTION_ID` | Azure subscription ID | `az account show --query id` |
| `ACR_USERNAME` | ACR admin username | `az acr credential show --name acrbilling --query username` |
| `ACR_PASSWORD` | ACR admin password | `az acr credential show --name acrbilling --query 'passwords[0].value'` |

### Step 4: Deploy via GitHub Actions

The deploy workflow runs automatically on push to `main`, or manually:

```bash
# Manual deployment via GitHub CLI
gh workflow run deploy.yml -f environment=dev

# Or use the GitHub UI:
# Actions → Deploy → Run workflow → Select environment (dev/staging/prod)
```

**Pipeline stages:**
1. **Build & Test** — Compiles each service, runs tests (matrix: 4 services in parallel)
2. **Build Containers** — Builds Docker images, pushes to ACR tagged with commit SHA
3. **Deploy** — Updates Azure Container Apps with new images

### Step 5: Verify Deployment

```bash
# Get the FQDN of deployed services
az containerapp show --name ca-user-service-dev \
  --resource-group rg-billing-dev \
  --query 'properties.configuration.ingress.fqdn' -o tsv

# Check health
curl https://<user-service-fqdn>/actuator/health
curl https://<customer-service-fqdn>/actuator/health
curl https://<billing-service-fqdn>/actuator/health
curl https://<reporting-service-fqdn>/actuator/health
```

### Data Migration (Derby → PostgreSQL)

To seed the new PostgreSQL databases from existing Derby data:

```bash
# Requires psql and the target database to be running
./scripts/migrate-data.sh
```

---

## API Contracts

All services return `{status, data, errors}` envelopes with RFC 7807 ProblemDetail for errors.

- [User API](specs/001-modernise-monolith/contracts/user-api.md) — CRUD, pagination, 409 on duplicate username
- [Customer API](specs/001-modernise-monolith/contracts/customer-api.md) — CRUD, 409 on delete with linked hours
- [Billing API](specs/001-modernise-monolith/contracts/billing-api.md) — Categories + Hours + Summary, Dapr events
- [Reporting API](specs/001-modernise-monolith/contracts/reporting-api.md) — Monthly, Range, Utilisation (CQRS)

---

## Project Structure

```
├── services/                           # Spring Boot microservices
│   ├── user-service/                   # User Management (port 8081)
│   ├── customer-service/               # Customer Management (port 8082)
│   ├── billing-service/                # Billing & Time Tracking (port 8083)
│   ├── reporting-service/              # Reporting — CQRS (port 8084)
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
│   ├── ci.yml                          # CI: compile → test → coverage gate
│   └── deploy.yml                      # CD: build → test → ACR → ACA deploy
├── docker-compose.yml                  # Local dev (4 services + PostgreSQL + Redis)
├── build.gradle                        # Root build with Spring Boot 3.2.2 BOM
└── settings.gradle                     # Multi-module configuration (6 subprojects)
```

---

## Configuration

### Microservices (Environment Variables)

| Variable | Default (Docker Compose) | Description |
|----------|--------------------------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres:5432/<servicedb>` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Database password |
| `SERVER_PORT` | `8080` | Container port (all services) |

### Legacy Monolith (Environment Variables)

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:derby:./data/bigbadmonolith;create=true` | Database JDBC URL |
| `DB_USER` | `app` | Database username |
| `DB_PASSWORD` | `app` | Database password |

---

## Legacy Monolith (Open Liberty)

The original monolith is preserved under `src/` with its test safety net:

```bash
./liberty-dev.sh                        # Start Liberty in dev mode
# Access: http://localhost:9080/big-bad-monolith/
```

---

## Modernisation Journey

| Phase | Status | Description |
|-------|--------|-------------|
| Phase 1-3 | ✅ Complete | Test infrastructure, CI pipeline, 85%+ coverage safety net |
| Phase 4 (US2) | ✅ Complete | Security fixes — XSS escaping, credential externalisation, error pages |
| Phase 5 (US3) | ✅ Complete | Architecture — Service interfaces, exception hierarchy, Flyway |
| Phase 6 (US4) | ✅ Complete | Date/Time — Joda-Time → java.time, Java 17+ target |
| Phase 7 (US5) | ✅ Complete | Performance — HikariCP pooling, pagination, N+1 elimination |
| Phase 8 (US6) | ✅ Complete | Decompose into 4 Spring Boot microservices on Azure Container Apps |
| Phase 9 | ✅ Complete | Polish — data migration, documentation, Dependabot |

---

## Documentation

| Document | Path |
|----------|------|
| Specification | [`specs/001-modernise-monolith/spec.md`](specs/001-modernise-monolith/spec.md) |
| Plan | [`specs/001-modernise-monolith/plan.md`](specs/001-modernise-monolith/plan.md) |
| Tasks | [`specs/001-modernise-monolith/tasks.md`](specs/001-modernise-monolith/tasks.md) |
| Data Model | [`specs/001-modernise-monolith/data-model.md`](specs/001-modernise-monolith/data-model.md) |
| API Contracts | [`specs/001-modernise-monolith/contracts/`](specs/001-modernise-monolith/contracts/) |
| Quickstart | [`specs/001-modernise-monolith/quickstart.md`](specs/001-modernise-monolith/quickstart.md) |
| Constitution | [`.specify/memory/constitution.md`](.specify/memory/constitution.md) |
| Runbooks | [`docs/runbooks/`](docs/runbooks/) |

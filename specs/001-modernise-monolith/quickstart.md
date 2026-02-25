# Quickstart: Modernise Monolith Billing Platform

**Final Output** | **Date**: 2026-02-24 | **Plan**: [plan.md](plan.md)

This guide enables a new developer to set up the development environment and run all services within 30 minutes (SC-006).

---

## Prerequisites

| Tool | Version | Installation |
|------|---------|-------------|
| Java JDK | 17+ (LTS) | [Microsoft Build of OpenJDK](https://learn.microsoft.com/java/openjdk/download) |
| Gradle | 8.x (via wrapper) | Included — use `./gradlew` or `.\gradlew.bat` |
| Git | 2.x+ | [git-scm.com](https://git-scm.com/) |
| Docker Desktop | 4.x+ | [docker.com](https://www.docker.com/products/docker-desktop/) (required for Docker Compose) |
| Azure CLI | 2.x+ | [Install Azure CLI](https://learn.microsoft.com/cli/azure/install-azure-cli) (deployment only) |

## Clone & Build (5 minutes)

```bash
# Clone the repository
git clone https://github.com/owainow/ai-sdlc-modernisation.git
cd ai-sdlc-modernisation

# Verify Java version
java -version
# Expected: openjdk version "17.x.x" or higher

# Build all modules (monolith + 4 microservices)
./gradlew clean build       # Linux/macOS
.\gradlew.bat clean build   # Windows
```

## Run Tests

```bash
# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "com.sourcegraph.demo.bigbadmonolith.dao.UserDAOTest"

# Run tests with coverage report
./gradlew test jacocoTestReport
# Report at: build/reports/jacoco/test/html/index.html
```

## Run the Application (Legacy — Phases 0–3)

```bash
# Start Liberty in dev mode (hot reload)
# Windows:
.\liberty-dev.bat

# Linux/macOS:
./liberty-dev.sh

# Or via Gradle:
./gradlew libertyDev
```

**Application URL**: http://localhost:9080/big-bad-monolith/

| Page | URL |
|------|-----|
| Home | http://localhost:9080/big-bad-monolith/ |
| Users | http://localhost:9080/big-bad-monolith/users.jsp |
| Customers | http://localhost:9080/big-bad-monolith/customers.jsp |
| Hours | http://localhost:9080/big-bad-monolith/hours.jsp |
| Categories | http://localhost:9080/big-bad-monolith/categories.jsp |
| Reports | http://localhost:9080/big-bad-monolith/reports.jsp |

## Run the Application (Phase 4 — Microservices)

### Option A: Docker Compose (Recommended — 5 minutes)

```bash
# Start all 4 services + PostgreSQL + Redis
docker compose up --build

# Services available at:
#   user-service:      http://localhost:8081/api/v1/users
#   customer-service:  http://localhost:8082/api/v1/customers
#   billing-service:   http://localhost:8083/api/v1/categories
#   reporting-service: http://localhost:8084/api/v1/reports

# Health checks:
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health

# Stop all services
docker compose down
```

### Option B: Individual Services (requires PostgreSQL running)

```bash
# Start PostgreSQL first
docker run -d --name postgres -p 5432:5432 \
  -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres \
  postgres:16-alpine

# Create databases
docker exec postgres psql -U postgres -c "CREATE DATABASE userdb;"
docker exec postgres psql -U postgres -c "CREATE DATABASE customerdb;"
docker exec postgres psql -U postgres -c "CREATE DATABASE billingdb;"
docker exec postgres psql -U postgres -c "CREATE DATABASE reportingdb;"

# Run individual services:
./gradlew :services:user-service:bootRun
./gradlew :services:customer-service:bootRun
./gradlew :services:billing-service:bootRun
./gradlew :services:reporting-service:bootRun
```

### Verify APIs

```bash
# List users (empty initially)
curl -s http://localhost:8081/api/v1/users | jq .

# Create a user
curl -s -X POST http://localhost:8081/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username":"jdoe","firstName":"John","lastName":"Doe","password":"Password1"}' | jq .

# Create a customer
curl -s -X POST http://localhost:8082/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{"name":"Acme Corporation"}' | jq .

# Create a billing category
curl -s -X POST http://localhost:8083/api/v1/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Development","hourlyRate":150.00}' | jq .
```

### Seed Data (Optional)

```bash
# Run the data migration script to seed sample data
./scripts/migrate-data.sh localhost 5432 postgres
```

## Project Structure

```
├── services/                           # Spring Boot microservices
│   ├── user-service/                   # User Management (port 8081)
│   ├── customer-service/               # Customer Management (port 8082)
│   ├── billing-service/                # Billing & Time Tracking (port 8083)
│   ├── reporting-service/              # Reporting CQRS (port 8084)
│   └── shared/
│       ├── common-dto/                 # ApiResponse, PaginatedResponse
│       └── common-test/               # TestDataFactory
├── src/                                # Legacy monolith (Open Liberty)
│   ├── main/java/                      # Application code
│   ├── main/resources/db/migration     # Flyway migrations
│   └── test/java/                      # Test code (85%+ coverage)
├── infra/                              # Azure Bicep IaC
│   ├── main.bicep                      # Orchestrator
│   ├── modules/                        # ACA, PostgreSQL, Key Vault, Redis, monitoring
│   └── parameters/                     # dev, staging, prod
├── tests/load/                         # k6 load test scripts
├── docs/runbooks/                      # Per-service operational runbooks
├── scripts/                            # Data migration scripts
├── docker-compose.yml                  # Local dev environment
├── build.gradle                        # Root build file (Spring Boot BOM)
└── settings.gradle                     # Multi-module settings
```

## Key Commands Reference

| Action | Command |
|--------|---------|
| Build all | `./gradlew clean build` |
| Test all | `./gradlew test` |
| Coverage report | `./gradlew test jacocoTestReport` |
| Coverage gate | `./gradlew jacocoTestCoverageVerification` |
| Compile only | `./gradlew compileJava` |
| Docker Compose up | `docker compose up --build` |
| Docker Compose down | `docker compose down` |
| User service test | `./gradlew :services:user-service:test` |
| Customer service test | `./gradlew :services:customer-service:test` |
| Billing service test | `./gradlew :services:billing-service:test` |
| Reporting service test | `./gradlew :services:reporting-service:test` |
| Liberty dev mode | `./gradlew libertyDev` (legacy monolith) |
| Stop Liberty | `./gradlew libertyStop` |

## Development Workflow

1. Create a feature branch from `main`: `git checkout -b <issue>-<description>`
2. Write failing tests first (Red).
3. Implement to make tests pass (Green).
4. Refactor while tests stay green (Refactor).
5. Ensure coverage thresholds are met: `./gradlew test jacocoTestReport`
6. Commit using conventional format: `feat(billing): add invoice endpoint`
7. Push and create a PR — CI must pass all quality gates.

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `JAVA_HOME` not set | Set to JDK 21 path: `export JAVA_HOME=/path/to/jdk-21` |
| Port 9080 in use | Stop existing Liberty: `./gradlew libertyStop` or kill the process |
| Derby lock file error | Delete `build/wlp/usr/servers/defaultServer/derby/` directory |
| Tests fail with Docker errors | Ensure Docker Desktop is running (needed for Testcontainers) |
| Gradle wrapper permission denied | Run `chmod +x gradlew` on Linux/macOS |

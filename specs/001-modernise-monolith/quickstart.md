# Quickstart: Modernise Monolith Billing Platform

**Phase 1 Output** | **Date**: 2026-02-23 | **Plan**: [plan.md](plan.md)

This guide enables a new developer to set up the development environment and run all tests within 30 minutes (SC-006).

---

## Prerequisites

| Tool | Version | Installation |
|------|---------|-------------|
| Java JDK | 21 (LTS) | [Microsoft Build of OpenJDK](https://learn.microsoft.com/java/openjdk/download) |
| Gradle | 8.x (via wrapper) | Included — use `./gradlew` or `.\gradlew.bat` |
| Git | 2.x+ | [git-scm.com](https://git-scm.com/) |
| Docker Desktop | 4.x+ | [docker.com](https://www.docker.com/products/docker-desktop/) (required for Testcontainers) |
| Azure CLI | 2.x+ | [Install Azure CLI](https://learn.microsoft.com/cli/azure/install-azure-cli) (Phase 4+) |
| Azure Developer CLI (azd) | Latest | [Install azd](https://learn.microsoft.com/azure/developer/azure-developer-cli/install-azd) (Phase 4+) |

## Clone & Build

```bash
# Clone the repository
git clone https://github.com/owainow/ai-sdlc-modernisation.git
cd ai-sdlc-modernisation

# Verify Java version
java -version
# Expected: openjdk version "21.x.x"

# Build the project
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

```bash
# Start all services locally with Docker Compose
docker compose up --build

# Or run individual services:
./gradlew :services:user-service:bootRun
./gradlew :services:customer-service:bootRun
./gradlew :services:billing-service:bootRun
./gradlew :services:reporting-service:bootRun
```

## Project Structure

```
├── specs/                          # Speckit specifications
│   └── 001-modernise-monolith/     # This feature
├── src/                            # Monolith source (Phases 0-3)
│   ├── main/java/                  # Application code
│   ├── main/resources/db/migration # Flyway migrations
│   └── test/java/                  # Test code
├── services/                       # Microservices (Phase 4)
│   ├── user-service/
│   ├── customer-service/
│   ├── billing-service/
│   └── reporting-service/
├── infra/                          # Azure Bicep IaC (Phase 4)
├── build.gradle                    # Root build file
└── settings.gradle                 # Multi-module settings
```

## Key Commands Reference

| Action | Command |
|--------|---------|
| Build | `./gradlew clean build` |
| Test | `./gradlew test` |
| Coverage | `./gradlew test jacocoTestReport` |
| Liberty dev mode | `./gradlew libertyDev` |
| Stop Liberty | `./gradlew libertyStop` |
| Compile only | `./gradlew compileJava` |

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

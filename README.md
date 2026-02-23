# Big Bad Monolith — Modernisation in Progress

A Jakarta EE billing platform undergoing modernisation from a JSP monolith to Spring Boot 3.x microservices on Azure Container Apps. This repository tracks the complete strangler fig migration journey.

## Modernisation Status

| Phase | Status | Description |
|-------|--------|-------------|
| Phase 1-3 | ✅ Complete | Test infrastructure, CI pipeline, 85%+ coverage safety net |
| Phase 4 (US2) | ✅ Complete | Security fixes — XSS escaping, credential externalisation, error pages |
| Phase 5 (US3) | ✅ Complete | Architecture — Service interfaces, exception hierarchy, Flyway |
| Phase 6 (US4) | ✅ Complete | Date/Time — Joda-Time → java.time, Java 17+ target |
| Phase 7 (US5) | ✅ Complete | Performance — HikariCP pooling, pagination, N+1 elimination |
| Phase 8 (US6) | 🔲 Pending | Decompose into 4 Spring Boot microservices |
| Phase 9 | 🔲 Pending | Polish — data migration, documentation, Dependabot |

## Features

- Service layer with interfaces and implementations (UserService, CustomerService, etc.)
- Exception hierarchy (ResourceNotFoundException, ValidationException, DuplicateResourceException)
- HikariCP connection pooling (replaces unused commons-dbcp2)
- Paginated query support (PaginationRequest + PaginatedResponse DTOs)
- XSS protection via HtmlUtils.htmlEscape() in all JSP pages
- Custom 404/500 error pages (no stack trace exposure)
- Credentials externalised via environment variables
- java.time exclusively (zero Joda-Time dependency)
- Thread-safe DateTimeFormatter (replaces SimpleDateFormat)
- N+1 query elimination in BillingService (batch category loading)
- Flyway migration (V1__initial_schema.sql)
- 85%+ test coverage with JaCoCo 80% gate
- GitHub Actions CI pipeline (compile → test → coverage → artifact upload)
- Dependabot for automated dependency updates

## Domain Model

### Core Entities
- **User**: Employees who log billable hours
- **Customer**: Companies billed for services
- **BillingCategory**: Work categories with hourly rates (e.g., Development $150/hr)
- **BillableHour**: Time entries with hours (BigDecimal), notes, date (java.time.LocalDate)

### Database Schema
```sql
users (id, email, name)
customers (id, name, email, address, created_at)
billing_categories (id, name, description, hourly_rate)
billable_hours (id, customer_id, user_id, category_id, hours, note, date_logged, created_at)
```

## Build & Run

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run tests with coverage report
./gradlew test jacocoTestReport

# Verify 80% coverage gate
./gradlew jacocoTestCoverageVerification

# Generate WAR file
./gradlew war
```

## Architecture

### Current State (Modernised Monolith)
- **Language**: Java 17+ on Open Liberty (Jakarta EE)
- **Database**: Embedded Apache Derby with HikariCP connection pooling
- **Service Layer**: Interface-based services with DI-ready constructors
- **Security**: XSS escaping, externalised credentials, custom error pages
- **Date/Time**: java.time exclusively (thread-safe)
- **Testing**: 85%+ line coverage, JUnit 5 + Mockito + AssertJ

### Target State (Phase 8)
- **Language**: Java 21 LTS on Spring Boot 3.x
- **Platform**: Azure Container Apps
- **Database**: Azure Database for PostgreSQL Flexible Server
- **Services**: 4 independently deployable microservices
- **Observability**: OpenTelemetry → Application Insights

### Target Microservices (4 Bounded Contexts)

| Service | Responsibilities | API Base |
|---------|-----------------|----------|
| User Management | User CRUD, authentication | `/api/v1/users` |
| Customer Management | Customer CRUD | `/api/v1/customers` |
| Billing & Time Tracking | Categories, Hours, Summary | `/api/v1/categories`, `/api/v1/hours` |
| Reporting (CQRS) | Monthly, Range, Utilisation reports | `/api/v1/reports` |

## Project Structure

```
src/main/java/com/sourcegraph/demo/bigbadmonolith/
├── dao/            DAO pattern with HikariCP pooled connections
├── dto/            PaginationRequest, PaginatedResponse
├── entity/         User, Customer, BillingCategory, BillableHour (java.time)
├── exception/      ResourceNotFound, DuplicateResource, Validation
├── service/        Interfaces + impl/ implementations
├── util/           DateTimeUtils (java.time), HtmlUtils (XSS escaping)
└── StartupListener.java

src/main/webapp/    JSP pages (XSS-escaped), custom error pages
src/main/resources/ Flyway migrations (db/migration/)
src/test/           85%+ coverage (entity, dao, service, integration, security tests)
.github/            CI workflow + Dependabot config
specs/              Modernisation spec, plan, tasks, contracts
```

## Running the Application

### Development Mode (Open Liberty)
```bash
./liberty-dev.sh        # Linux/macOS
liberty-dev.bat         # Windows
```

### Manual Deployment
```bash
./gradlew build
./gradlew libertyStart  # Start Liberty server
./gradlew libertyStop   # Stop Liberty server
```

### Access Points
- **HTTP**: `http://localhost:9080/big-bad-monolith/`
- **HTTPS**: `https://localhost:9443/big-bad-monolith/`

## Configuration

### Environment Variables
| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:derby:./data/bigbadmonolith;create=true` | Database JDBC URL |
| `DB_USER` | `app` | Database username |
| `DB_PASSWORD` | `app` | Database password |

## Specs & Documentation

- **Spec**: `specs/001-modernise-monolith/spec.md`
- **Plan**: `specs/001-modernise-monolith/plan.md`
- **Tasks**: `specs/001-modernise-monolith/tasks.md`
- **API Contracts**: `specs/001-modernise-monolith/contracts/`
- **Constitution**: `.specify/memory/constitution.md`

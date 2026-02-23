# Tasks: Modernise Monolith Billing Platform

**Input**: Design documents from `/specs/001-modernise-monolith/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅, quickstart.md ✅

**Tests**: Included — User Story 1 explicitly requires test coverage (P1), and the constitution mandates Test-First Development (NON-NEGOTIABLE). All subsequent stories follow Red-Green-Refactor.

**Organization**: Tasks grouped by user story to enable independent implementation and testing. User stories map to the spec's migration phases: US1=Phase 0 (Safety Net), US2=Phase 1 (Security), US3=Phase 2 (Architecture), US4/US5=Phase 3 (Modernise), US6=Phase 4 (Decompose).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Source**: `src/main/java/com/sourcegraph/demo/bigbadmonolith/` (abbrev: `src/.../`)
- **Tests**: `src/test/java/com/sourcegraph/demo/bigbadmonolith/` (abbrev: `test/.../`)
- **Webapp**: `src/main/webapp/`
- **Config**: `src/main/liberty/config/`
- **Migrations**: `src/main/resources/db/migration/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialisation — add test framework dependencies, coverage tooling, test directory structure, and CI pipeline.

- [ ] T001 Add test dependencies to build.gradle — Mockito (`org.mockito:mockito-core:5.x`), AssertJ (`org.assertj:assertj-core:3.x`), Testcontainers (`org.testcontainers:junit-jupiter:1.x`), REST Assured (`io.rest-assured:rest-assured:5.x`), and JaCoCo plugin for coverage reporting in build.gradle
- [ ] T002 [P] Create test source directory structure under src/test/java/com/sourcegraph/demo/bigbadmonolith/ with sub-packages: entity/, dao/, service/, util/, security/, integration/
- [ ] T003 [P] Create GitHub Actions CI workflow with stages: compile → test → coverage check → security scan in .github/workflows/ci.yml
- [ ] T004 [P] Configure JaCoCo coverage verification rule in build.gradle — fail build if line coverage < 80%

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core test infrastructure that MUST be complete before ANY user story can be implemented.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T005 Create test database configuration helper that initialises an in-memory Derby database with the same schema as production (mirroring DatabaseService.initializeDatabase) in src/test/java/com/sourcegraph/demo/bigbadmonolith/TestDatabaseConfig.java
- [ ] T006 [P] Create test data factory with builder methods for creating valid User, Customer, BillingCategory, and BillableHour instances with sensible defaults in src/test/java/com/sourcegraph/demo/bigbadmonolith/TestDataFactory.java
- [ ] T007 [P] Create base integration test abstract class that sets up and tears down the in-memory Derby database per test class, providing a shared Connection for DAO tests in src/test/java/com/sourcegraph/demo/bigbadmonolith/integration/BaseIntegrationTest.java

**Checkpoint**: Foundation ready — user story implementation can now begin.

---

## Phase 3: User Story 1 — Establish a Safety Net of Automated Tests (Priority: P1) 🎯 MVP

**Goal**: Achieve 80%+ overall test coverage and 95%+ on critical billing/auth paths by writing characterisation tests that capture the current behaviour of every DAO, service, entity, and utility before any refactoring begins.

**Independent Test**: Run `./gradlew test jacocoTestReport` — all tests pass and the JaCoCo HTML report at `build/reports/jacoco/test/html/index.html` shows ≥80% overall line coverage and ≥95% on BillingService and authentication-related code.

### Tests — Entity Characterisation (document current behaviour)

- [ ] T008 [P] [US1] Write characterisation tests for User entity covering constructor, getters, setters, and current equals/hashCode behaviour in src/test/java/com/sourcegraph/demo/bigbadmonolith/entity/UserTest.java
- [ ] T009 [P] [US1] Write characterisation tests for Customer entity covering constructor, getters, setters, and Joda-Time DateTime field behaviour in src/test/java/com/sourcegraph/demo/bigbadmonolith/entity/CustomerTest.java
- [ ] T010 [P] [US1] Write characterisation tests for BillingCategory entity covering constructor, getters, setters, and hourly rate storage (including edge cases: zero, negative) in src/test/java/com/sourcegraph/demo/bigbadmonolith/entity/BillingCategoryTest.java
- [ ] T011 [P] [US1] Write characterisation tests for BillableHour entity covering constructor, getters, setters, foreign key ID storage, and Joda-Time date handling in src/test/java/com/sourcegraph/demo/bigbadmonolith/entity/BillableHourTest.java

### Tests — DAO Characterisation (document current CRUD behaviour)

- [ ] T012 [P] [US1] Write characterisation tests for UserDAO covering create, findById, findAll, update, delete operations against in-memory Derby in src/test/java/com/sourcegraph/demo/bigbadmonolith/dao/UserDAOTest.java
- [ ] T013 [P] [US1] Write characterisation tests for CustomerDAO covering create, findById, findAll, update, delete operations against in-memory Derby in src/test/java/com/sourcegraph/demo/bigbadmonolith/dao/CustomerDAOTest.java
- [ ] T014 [P] [US1] Write characterisation tests for BillingCategoryDAO covering create, findById, findAll, update, delete operations against in-memory Derby in src/test/java/com/sourcegraph/demo/bigbadmonolith/dao/BillingCategoryDAOTest.java
- [ ] T015 [P] [US1] Write characterisation tests for BillableHourDAO covering create, findById, findAll, findByUser, findByCustomer, update, delete operations against in-memory Derby in src/test/java/com/sourcegraph/demo/bigbadmonolith/dao/BillableHourDAOTest.java
- [ ] T016 [P] [US1] Write characterisation tests for ConnectionManager covering getConnection and connection lifecycle (documenting the hardcoded credential pattern) in src/test/java/com/sourcegraph/demo/bigbadmonolith/dao/ConnectionManagerTest.java

### Tests — Service Characterisation (document current business logic)

- [ ] T017 [US1] Write characterisation tests for BillingService covering all public methods including billing summary calculation, N+1 query behaviour documentation, and edge cases (empty data, missing references) in src/test/java/com/sourcegraph/demo/bigbadmonolith/service/BillingServiceTest.java
- [ ] T018 [P] [US1] Write characterisation tests for DatabaseService covering initializeDatabase and schema creation verification in src/test/java/com/sourcegraph/demo/bigbadmonolith/service/DatabaseServiceTest.java
- [ ] T019 [P] [US1] Write characterisation tests for DataInitializationService covering seed data creation and idempotency behaviour in src/test/java/com/sourcegraph/demo/bigbadmonolith/service/DataInitializationServiceTest.java

### Tests — Utility Characterisation

- [ ] T020 [P] [US1] Write characterisation tests for DateTimeUtils covering all formatting/parsing methods, documenting the thread-unsafe static SimpleDateFormat pattern, and Joda-Time dependency in src/test/java/com/sourcegraph/demo/bigbadmonolith/util/DateTimeUtilsTest.java

### Tests — Integration (end-to-end flows)

- [ ] T021 [US1] Write integration test for full CRUD lifecycle — create User → create Customer → create BillingCategory → create BillableHour → verify relationships → delete in reverse order — in src/test/java/com/sourcegraph/demo/bigbadmonolith/integration/CrudLifecycleIntegrationTest.java
- [ ] T022 [US1] Write integration test for startup initialisation flow — verify StartupListener triggers DataInitializationService and seed data is present in src/test/java/com/sourcegraph/demo/bigbadmonolith/integration/StartupInitialisationIntegrationTest.java

### Coverage Verification

- [ ] T023 [US1] Run full test suite with `./gradlew test jacocoTestReport`, verify 80%+ overall line coverage and 95%+ on BillingService. Document any coverage gaps that need attention in later phases.

**Checkpoint**: Safety net established. All existing behaviour is documented by tests. Refactoring can now proceed with regression safety.

---

## Phase 4: User Story 2 — Eliminate Critical Security Vulnerabilities (Priority: P1)

**Goal**: Fix all XSS, CSRF, and credential exposure vulnerabilities. Tests prove each vulnerability exists before the fix, then prove the fix resolves it.

**Independent Test**: Attempt known XSS payloads (`<script>alert(1)</script>` in customer name), CSRF attacks (POST without token), and grep for hardcoded credentials — all attacks are blocked and zero credentials remain in source.

### Tests — Security Vulnerability Proof (Red phase: tests MUST fail initially)

- [ ] T024 [P] [US2] Write XSS vulnerability tests that verify stored user input containing `<script>` tags is properly escaped in HTML output for each JSP page in src/test/java/com/sourcegraph/demo/bigbadmonolith/security/XssVulnerabilityTest.java
- [ ] T025 [P] [US2] Write CSRF vulnerability tests that verify state-changing operations (create, update, delete) reject requests without a valid CSRF token in src/test/java/com/sourcegraph/demo/bigbadmonolith/security/CsrfVulnerabilityTest.java
- [ ] T026 [P] [US2] Write credential exposure tests that scan source files for hardcoded database passwords, connection strings, and embedded credentials — asserting zero occurrences in src/test/java/com/sourcegraph/demo/bigbadmonolith/security/CredentialExposureTest.java
- [ ] T027 [P] [US2] Write error page tests that verify application errors return custom error pages without stack traces, internal paths, or server details in src/test/java/com/sourcegraph/demo/bigbadmonolith/security/ErrorPageTest.java

### Implementation — XSS Remediation

- [ ] T028 [P] [US2] Sanitise all dynamic output in src/main/webapp/users.jsp — escape user-provided values (username, firstName, lastName) using JSTL `<c:out>` or `fn:escapeXml()`
- [ ] T029 [P] [US2] Sanitise all dynamic output in src/main/webapp/customers.jsp — escape customer name using JSTL `<c:out>` or `fn:escapeXml()`
- [ ] T030 [P] [US2] Sanitise all dynamic output in src/main/webapp/categories.jsp — escape category name and hourly rate display using JSTL `<c:out>` or `fn:escapeXml()`
- [ ] T031 [P] [US2] Sanitise all dynamic output in src/main/webapp/hours.jsp — escape all rendered field values using JSTL `<c:out>` or `fn:escapeXml()`
- [ ] T032 [P] [US2] Sanitise all dynamic output in src/main/webapp/reports.jsp — escape all rendered values and remove raw JDBC connection code using JSTL `<c:out>` or `fn:escapeXml()`
- [ ] T033 [P] [US2] Sanitise all dynamic output in src/main/webapp/index.jsp — escape any rendered values using JSTL `<c:out>` or `fn:escapeXml()`

### Implementation — CSRF Protection

- [ ] T034 [US2] Add CSRF token generation and validation for all state-changing form submissions across all JSP pages and configure a servlet filter for CSRF enforcement in src/main/webapp/WEB-INF/web.xml and JSP forms

### Implementation — Credential Externalisation

- [ ] T035 [US2] Refactor ConnectionManager to read database credentials from environment variables or JNDI instead of hardcoded values in src/main/java/com/sourcegraph/demo/bigbadmonolith/dao/ConnectionManager.java
- [ ] T036 [US2] Remove hardcoded credentials from server.xml and reference environment variables or bootstrap.properties for sensitive values in src/main/liberty/config/server.xml
- [ ] T037 [US2] Remove direct DriverManager.getConnection() with hardcoded credentials from reports.jsp — delegate to ConnectionManager/LibertyConnectionManager in src/main/webapp/reports.jsp

### Implementation — Error Handling

- [ ] T038 [P] [US2] Create custom error pages for 404 and 500 errors that display user-friendly messages without exposing internals in src/main/webapp/WEB-INF/error/404.html and src/main/webapp/WEB-INF/error/500.html
- [ ] T039 [US2] Configure error page mappings in web.xml to route HTTP 404, 500, and java.lang.Exception to custom error pages in src/main/webapp/WEB-INF/web.xml

### Implementation — Input Validation

- [ ] T040 [US2] Add input validation for all user-provided data (customer names, billing category rates, hours, dates) at the service layer boundary — reject invalid input with meaningful error messages in src/main/java/com/sourcegraph/demo/bigbadmonolith/service/

### Verification

- [ ] T041 [US2] Run all security tests and verify they pass — XSS payloads are escaped, CSRF attacks are blocked, zero hardcoded credentials remain, error pages hide internals

**Checkpoint**: All critical security vulnerabilities eliminated. The application is safe to expose to users.

---

## Phase 5: User Story 3 — Separate Business Logic from Presentation (Priority: P2)

**Goal**: Extract all business logic from JSP pages into service interfaces with dependency injection, create a clean controller layer, introduce structured logging, and set up Flyway migrations — so that each layer can be tested, maintained, and evolved independently.

**Independent Test**: Grep all JSP files for `new *DAO(`, `DriverManager`, `getConnection` — zero occurrences. All business logic is accessible through service interfaces. Unit tests run without a web server.

### Tests — Service Interface Contracts (Red phase)

- [ ] T042 [P] [US3] Write contract tests for UserService interface — CRUD operations, validation rules, not-found handling — in src/test/java/com/sourcegraph/demo/bigbadmonolith/service/UserServiceTest.java
- [ ] T043 [P] [US3] Write contract tests for CustomerService interface — CRUD operations, unique name validation, delete-with-linked-hours rejection — in src/test/java/com/sourcegraph/demo/bigbadmonolith/service/CustomerServiceTest.java
- [ ] T044 [P] [US3] Write contract tests for BillingCategoryService interface — CRUD operations, rate validation (> 0, ≤ 10000), delete-with-linked-hours rejection — in src/test/java/com/sourcegraph/demo/bigbadmonolith/service/BillingCategoryServiceTest.java
- [ ] T045 [P] [US3] Write contract tests for ReportingService interface — monthly report, date range report, utilisation report, edge cases (empty data, future dates) — in src/test/java/com/sourcegraph/demo/bigbadmonolith/service/ReportingServiceTest.java
- [ ] T046 [P] [US3] Write contract tests for BillableHourService interface — CRUD operations, hours validation (> 0, ≤ 24), 24-hour daily cap per user, work date not in future — in src/test/java/com/sourcegraph/demo/bigbadmonolith/service/BillableHourServiceTest.java

### Implementation — Service Interfaces

- [ ] T047 [P] [US3] Create UserService interface defining CRUD operations with typed parameters and return types in src/main/java/com/sourcegraph/demo/bigbadmonolith/service/UserService.java
- [ ] T048 [P] [US3] Create CustomerService interface defining CRUD operations with unique name validation in src/main/java/com/sourcegraph/demo/bigbadmonolith/service/CustomerService.java
- [ ] T049 [P] [US3] Create BillingCategoryService interface defining CRUD operations with rate validation in src/main/java/com/sourcegraph/demo/bigbadmonolith/service/BillingCategoryService.java
- [ ] T050 [P] [US3] Create BillableHourService interface defining CRUD operations with hours and date validation in src/main/java/com/sourcegraph/demo/bigbadmonolith/service/BillableHourService.java
- [ ] T051 [P] [US3] Create ReportingService interface defining monthly, date range, and utilisation report methods in src/main/java/com/sourcegraph/demo/bigbadmonolith/service/ReportingService.java

### Implementation — Service Implementations

- [ ] T052 [P] [US3] Create UserServiceImpl implementing UserService, injecting UserDAO via CDI in src/main/java/com/sourcegraph/demo/bigbadmonolith/service/impl/UserServiceImpl.java
- [ ] T053 [P] [US3] Create CustomerServiceImpl implementing CustomerService, injecting CustomerDAO via CDI in src/main/java/com/sourcegraph/demo/bigbadmonolith/service/impl/CustomerServiceImpl.java
- [ ] T054 [P] [US3] Create BillingCategoryServiceImpl implementing BillingCategoryService, injecting BillingCategoryDAO via CDI in src/main/java/com/sourcegraph/demo/bigbadmonolith/service/impl/BillingCategoryServiceImpl.java
- [ ] T055 [P] [US3] Create BillableHourServiceImpl implementing BillableHourService, injecting BillableHourDAO via CDI in src/main/java/com/sourcegraph/demo/bigbadmonolith/service/impl/BillableHourServiceImpl.java
- [ ] T056 [US3] Create ReportingServiceImpl implementing ReportingService — extract all SQL logic from reports.jsp into this service, injecting DAOs via CDI in src/main/java/com/sourcegraph/demo/bigbadmonolith/service/impl/ReportingServiceImpl.java

### Implementation — Exception Handling & DTOs

- [ ] T057 [P] [US3] Create exception hierarchy: ResourceNotFoundException, DuplicateResourceException, ValidationException in src/main/java/com/sourcegraph/demo/bigbadmonolith/exception/
- [ ] T058 [P] [US3] Create request/response DTOs for user, customer, category, hours, and report operations in src/main/java/com/sourcegraph/demo/bigbadmonolith/dto/

### Implementation — JSP Refactoring (remove direct DAO access)

- [ ] T059 [US3] Refactor users.jsp to use injected UserService instead of `new UserDAO()` — delegate all CRUD operations to the service layer in src/main/webapp/users.jsp
- [ ] T060 [US3] Refactor customers.jsp to use injected CustomerService instead of `new CustomerDAO()` in src/main/webapp/customers.jsp
- [ ] T061 [US3] Refactor categories.jsp to use injected BillingCategoryService instead of `new BillingCategoryDAO()` in src/main/webapp/categories.jsp
- [ ] T062 [US3] Refactor hours.jsp to use injected BillableHourService instead of `new BillableHourDAO()` in src/main/webapp/hours.jsp
- [ ] T063 [US3] Refactor reports.jsp to use injected ReportingService — remove all raw JDBC code, SQL queries, and DriverManager calls in src/main/webapp/reports.jsp

### Implementation — Dependency Injection & Logging

- [ ] T064 [US3] Configure CDI bean discovery in beans.xml, ensure all service implementations and DAOs are CDI-managed beans in src/main/resources/META-INF/beans.xml
- [ ] T065 [US3] Add structured JSON logging using SLF4J + Logback with correlation ID support — add Logback dependency to build.gradle and create logback.xml configuration in src/main/resources/logback.xml

### Implementation — Database Migrations

- [ ] T066 [US3] Add Flyway dependency to build.gradle and create initial migration V1__initial_schema.sql capturing the current Derby schema as repeatable DDL in src/main/resources/db/migration/V1__initial_schema.sql

### Verification

- [ ] T067 [US3] Verify zero JSP files contain `new *DAO(`, `DriverManager`, `getConnection`, or raw SQL — all business logic routes through service interfaces. Run all tests to confirm no regressions.

**Checkpoint**: Clean architecture established. All layers are independently testable. Structured logging active. Migration framework in place.

---

## Phase 6: User Story 4 — Modernise Date/Time Handling and Language Level (Priority: P3)

**Goal**: Replace all Joda-Time usage with `java.time.*`, eliminate the thread-unsafe static `SimpleDateFormat`, and upgrade the project to compile on Java 17+ (targeting Java 21).

**Independent Test**: Search codebase for `import org.joda.time` and `import java.text.SimpleDateFormat` — zero occurrences. Run `./gradlew build` with Java 17+ — clean compilation with zero warnings.

### Tests (Red phase)

- [ ] T068 [P] [US4] Write tests verifying DateTimeUtils methods produce identical output when migrated to java.time.DateTimeFormatter — test thread-safety with concurrent access in src/test/java/com/sourcegraph/demo/bigbadmonolith/util/DateTimeUtilsMigrationTest.java
- [ ] T069 [P] [US4] Write tests verifying Customer entity uses java.time.Instant for createdAt/updatedAt instead of Joda DateTime in src/test/java/com/sourcegraph/demo/bigbadmonolith/entity/CustomerMigrationTest.java
- [ ] T070 [P] [US4] Write tests verifying BillableHour entity uses java.time.LocalDate for workDate instead of Joda date types in src/test/java/com/sourcegraph/demo/bigbadmonolith/entity/BillableHourMigrationTest.java

### Implementation

- [ ] T071 [US4] Migrate DateTimeUtils from Joda-Time to java.time.* — replace SimpleDateFormat with DateTimeFormatter (thread-safe, immutable), replace all Joda DateTime/LocalDate with java.time equivalents in src/main/java/com/sourcegraph/demo/bigbadmonolith/util/DateTimeUtils.java
- [ ] T072 [P] [US4] Migrate Customer entity from Joda DateTime to java.time.Instant for createdAt field, update constructor and getters in src/main/java/com/sourcegraph/demo/bigbadmonolith/entity/Customer.java
- [ ] T073 [P] [US4] Migrate BillableHour entity date handling from Joda to java.time.LocalDate for workDate, update all date-related methods in src/main/java/com/sourcegraph/demo/bigbadmonolith/entity/BillableHour.java
- [ ] T074 [US4] Update DataInitializationService to use java.time.* APIs for all date/time operations in src/main/java/com/sourcegraph/demo/bigbadmonolith/service/DataInitializationService.java
- [ ] T075 [US4] Update DAO classes that handle date/time columns to use java.time types in JDBC setters/getters (setTimestamp/setDate with java.time conversions) in src/main/java/com/sourcegraph/demo/bigbadmonolith/dao/
- [ ] T076 [US4] Update build.gradle — set sourceCompatibility and targetCompatibility to '17' (minimum), remove Joda-Time dependency `joda-time:joda-time:2.12.5` in build.gradle
- [ ] T077 [US4] Verify zero Joda-Time imports remain (`grep -r "org.joda.time" src/`), application compiles on Java 17+, all tests pass

**Checkpoint**: Codebase uses modern java.time exclusively. Thread-unsafe formatters eliminated. Java 17+ target set.

---

## Phase 7: User Story 5 — Improve Data Access Performance (Priority: P3)

**Goal**: Eliminate N+1 queries, add pagination to all list operations, and configure connection pooling — so the application performs efficiently under growing data volumes.

**Independent Test**: Load 1,000 billable hours across 50 customers, generate billing summary, count SQL queries (should be O(entity types) not O(rows)). Request paginated lists — results return only the requested page. Verify connection pool is active via logging.

### Tests (Red phase)

- [ ] T078 [P] [US5] Write tests for paginated DAO methods — verify page size, page number, total count, and boundary conditions (empty page, last page) in src/test/java/com/sourcegraph/demo/bigbadmonolith/dao/PaginationTest.java
- [ ] T079 [P] [US5] Write tests proving N+1 queries are eliminated in BillingService — mock DAO layer and verify batch query calls instead of per-item queries in src/test/java/com/sourcegraph/demo/bigbadmonolith/service/BillingServicePerformanceTest.java
- [ ] T080 [P] [US5] Write tests verifying HikariCP connection pooling is active and connections are reused across multiple DAO calls in src/test/java/com/sourcegraph/demo/bigbadmonolith/dao/ConnectionPoolingTest.java

### Implementation

- [ ] T081 [US5] Add HikariCP dependency to build.gradle and configure connection pooling in ConnectionManager — replace per-request connection creation with pool-managed connections in build.gradle and src/main/java/com/sourcegraph/demo/bigbadmonolith/dao/ConnectionManager.java
- [ ] T082 [US5] Add pagination support (page, size parameters) to UserDAO.findAll() — use SQL OFFSET/FETCH or LIMIT with total count query in src/main/java/com/sourcegraph/demo/bigbadmonolith/dao/UserDAO.java
- [ ] T083 [P] [US5] Add pagination support to CustomerDAO.findAll() in src/main/java/com/sourcegraph/demo/bigbadmonolith/dao/CustomerDAO.java
- [ ] T084 [P] [US5] Add pagination support to BillingCategoryDAO.findAll() in src/main/java/com/sourcegraph/demo/bigbadmonolith/dao/BillingCategoryDAO.java
- [ ] T085 [P] [US5] Add pagination support to BillableHourDAO.findAll() and filter methods in src/main/java/com/sourcegraph/demo/bigbadmonolith/dao/BillableHourDAO.java
- [ ] T086 [US5] Refactor BillingService to use batch/join queries for billing summary — eliminate N+1 patterns by fetching related data in bulk queries in src/main/java/com/sourcegraph/demo/bigbadmonolith/service/BillingService.java
- [ ] T087 [US5] Create PaginationRequest and PaginatedResponse DTOs for consistent pagination across all list endpoints in src/main/java/com/sourcegraph/demo/bigbadmonolith/dto/PaginationRequest.java and PaginatedResponse.java
- [ ] T088 [US5] Verify all list operations return paginated results, N+1 queries are eliminated, and connection pooling is active. Run all tests.

**Checkpoint**: Performance foundations in place. Queries are efficient, results are paginated, connections are pooled. Ready for decomposition.

---

## Phase 8: User Story 6 — Decompose into Independent Services (Priority: P4)

**Goal**: Migrate from Jakarta EE on Liberty to Spring Boot 3.x, decompose the monolith into four independently deployable microservices (User, Customer, Billing, Reporting), deploy to Azure Container Apps with full observability, CI/CD, and IaC.

**Independent Test**: Deploy each service independently. Call `GET /api/v1/users` on the User Service — returns paginated users. Create a billable hour via Billing Service — Reporting Service receives the event and updates its read model within 5 seconds. Each service's `/actuator/health` returns UP.

### Spring Boot Migration (from Liberty/Jakarta EE)

- [ ] T089 [US6] Create Gradle multi-module project layout — update settings.gradle to include `services/user-service`, `services/customer-service`, `services/billing-service`, `services/reporting-service`, `services/shared/common-dto`, `services/shared/common-test` in settings.gradle
- [ ] T090 [US6] Create root build.gradle configuration for multi-module project with shared dependency management (Spring Boot 3.x BOM, Java 21, shared plugins) in build.gradle
- [ ] T091 [P] [US6] Create common-dto module with shared DTOs (ApiResponse envelope, PaginatedResponse, RFC 7807 ProblemDetail, pagination types) in services/shared/common-dto/
- [ ] T092 [P] [US6] Create common-test module with shared test utilities (TestDataFactory, base test configuration, Testcontainers PostgreSQL setup) in services/shared/common-test/

### Contract Tests per API (Red phase — tests MUST fail before services exist)

- [ ] T093 [P] [US6] Write REST contract tests for User API per contracts/user-api.md — all CRUD endpoints, pagination, RFC 7807 errors, 409 on duplicate username in services/user-service/src/test/java/.../user/controller/UserControllerTest.java
- [ ] T094 [P] [US6] Write REST contract tests for Customer API per contracts/customer-api.md — all CRUD endpoints, 409 on delete with linked hours in services/customer-service/src/test/java/.../customer/controller/CustomerControllerTest.java
- [ ] T095 [P] [US6] Write REST contract tests for Billing API per contracts/billing-api.md — Categories CRUD, Hours CRUD, Billing Summary, validation rules in services/billing-service/src/test/java/.../billing/controller/BillingControllerTest.java
- [ ] T096 [P] [US6] Write REST contract tests for Reporting API per contracts/reporting-api.md — Monthly, Range, Utilisation reports, 503 fallback in services/reporting-service/src/test/java/.../reporting/controller/ReportingControllerTest.java

### User Service Implementation

- [ ] T097 [P] [US6] Create User Service Spring Boot application with main class, application.yml (PostgreSQL, Flyway, Actuator, OpenTelemetry), and Dockerfile in services/user-service/
- [ ] T098 [P] [US6] Create User JPA entity with UUID primary key, Bean Validation annotations, audit timestamps, equals/hashCode per data-model.md in services/user-service/src/main/java/.../user/entity/User.java
- [ ] T099 [US6] Create UserRepository (Spring Data JPA), UserService, and UserServiceImpl with BCrypt password hashing in services/user-service/src/main/java/.../user/
- [ ] T100 [US6] Create UserController implementing REST API per contracts/user-api.md — CRUD endpoints with pagination, RFC 7807 error handling, ApiResponse envelope in services/user-service/src/main/java/.../user/controller/UserController.java
- [ ] T101 [US6] Create Flyway migration V1__create_users.sql with UUID PK, unique username constraint, audit columns in services/user-service/src/main/resources/db/migration/V1__create_users.sql

### Customer Service Implementation

- [ ] T102 [P] [US6] Create Customer Service Spring Boot application with main class, application.yml, and Dockerfile in services/customer-service/
- [ ] T103 [P] [US6] Create Customer JPA entity with UUID PK, Bean Validation, audit timestamps per data-model.md in services/customer-service/src/main/java/.../customer/entity/Customer.java
- [ ] T104 [US6] Create CustomerRepository, CustomerService, and CustomerServiceImpl with delete-guard (reject if linked billable hours exist via inter-service check) in services/customer-service/src/main/java/.../customer/
- [ ] T105 [US6] Create CustomerController implementing REST API per contracts/customer-api.md in services/customer-service/src/main/java/.../customer/controller/CustomerController.java
- [ ] T106 [US6] Create Flyway migration V1__create_customers.sql in services/customer-service/src/main/resources/db/migration/V1__create_customers.sql

### Billing & Time Tracking Service Implementation

- [ ] T107 [P] [US6] Create Billing Service Spring Boot application with main class, application.yml (includes Dapr pub/sub config), and Dockerfile in services/billing-service/
- [ ] T108 [P] [US6] Create BillingCategory and BillableHour JPA entities with UUID PKs, Bean Validation, FK references by UUID per data-model.md in services/billing-service/src/main/java/.../billing/entity/
- [ ] T109 [US6] Create BillingCategoryRepository, BillableHourRepository, BillingService, and BillingServiceImpl with 24-hour daily cap validation in services/billing-service/src/main/java/.../billing/
- [ ] T110 [US6] Create BillingController implementing REST API per contracts/billing-api.md — Categories CRUD, Hours CRUD, Billing Summary in services/billing-service/src/main/java/.../billing/controller/BillingController.java
- [ ] T111 [US6] Implement Dapr pub/sub event publishing for hour.created, hour.updated, hour.deleted events per contracts/billing-api.md in services/billing-service/src/main/java/.../billing/event/BillingEventPublisher.java
- [ ] T112 [US6] Create Flyway migrations V1__create_billing_categories.sql and V2__create_billable_hours.sql in services/billing-service/src/main/resources/db/migration/

### Reporting Service Implementation (CQRS)

- [ ] T113 [P] [US6] Create Reporting Service Spring Boot application with main class, application.yml (Dapr subscription config), and Dockerfile in services/reporting-service/
- [ ] T114 [US6] Create read-optimised projection entities and ReportRepository with aggregate queries for monthly, range, and utilisation reports in services/reporting-service/src/main/java/.../reporting/
- [ ] T115 [US6] Implement Dapr pub/sub event consumer subscribing to billing-events topic — update read model on hour.created/updated/deleted in services/reporting-service/src/main/java/.../reporting/event/BillingEventConsumer.java
- [ ] T116 [US6] Create ReportingController implementing REST API per contracts/reporting-api.md — Monthly, Range, Utilisation endpoints with 503 fallback in services/reporting-service/src/main/java/.../reporting/controller/ReportingController.java
- [ ] T117 [US6] Create Flyway migration V1__create_reporting_read_model.sql for denormalised read model tables in services/reporting-service/src/main/resources/db/migration/V1__create_reporting_read_model.sql

### Cross-Cutting Concerns (all services)

- [ ] T118 [P] [US6] Configure Spring Security with Azure Entra ID (OAuth2 Resource Server) and RBAC scope enforcement per API contracts in each service's SecurityConfig.java
- [ ] T119 [P] [US6] Configure OpenTelemetry auto-instrumentation with Application Insights exporter — add `-javaagent` to Dockerfiles and configure APPLICATIONINSIGHTS_CONNECTION_STRING in each service's application.yml
- [ ] T120 [P] [US6] Configure Spring Cache with Redis (`spring-boot-starter-data-redis`) and TTL policies per research.md Decision 11 (users 15min, categories 30min, customers 10min, reports 5min) in each relevant service
- [ ] T121 [P] [US6] Configure Spring Cloud Azure App Configuration for feature flags per research.md Decision 12 in each service's application.yml
- [ ] T122 [P] [US6] Add Spring Actuator health endpoints (`/actuator/health/liveness`, `/actuator/health/readiness`) and Micrometer metrics in each service's application.yml

### Infrastructure & Deployment

- [ ] T123 [US6] Create Docker Compose configuration for local development — all 4 services, PostgreSQL, Redis, Dapr sidecars in docker-compose.yml
- [ ] T124 [P] [US6] Create Bicep main module orchestrating all infrastructure resources in infra/main.bicep
- [ ] T125 [P] [US6] Create Bicep module for Azure Container Apps environment and per-service container apps with resource limits per research.md Decision 14 in infra/modules/container-apps.bicep
- [ ] T126 [P] [US6] Create Bicep module for Azure Database for PostgreSQL Flexible Server with per-service schemas in infra/modules/postgresql.bicep
- [ ] T127 [P] [US6] Create Bicep module for Azure Key Vault with Managed Identity access policies in infra/modules/keyvault.bicep
- [ ] T128 [P] [US6] Create Bicep module for Azure Monitor workspace, Application Insights, and alert rules per research.md Decision 14 in infra/modules/monitoring.bicep
- [ ] T129 [P] [US6] Create Bicep module for Azure Cache for Redis in infra/modules/redis.bicep
- [ ] T130 [P] [US6] Create Bicep parameter files for dev, staging, and production environments in infra/parameters/dev.bicepparam, staging.bicepparam, prod.bicepparam
- [ ] T131 [US6] Create GitHub Actions deployment workflow — build all services → test → security scan → build containers → push to ACR → deploy to ACA (blue-green via revision labels) in .github/workflows/deploy.yml

### Load Testing & Runbooks

- [ ] T132 [P] [US6] Create k6 load test scripts for API endpoint response time (200ms p95), report generation (3s), and billing summary (2s) per research.md Decision 13 in tests/load/
- [ ] T133 [P] [US6] Create runbook template and per-service runbooks documenting common failure modes, diagnostic steps (log queries, health endpoints), and recovery procedures in docs/runbooks/

### Verification

- [ ] T134 [US6] Run all contract tests across all 4 services — verify API responses match contracts. Run Docker Compose locally — verify inter-service communication via Dapr. Verify health endpoints return UP.

**Checkpoint**: Full microservices architecture deployed. Four independent services communicating via Dapr, backed by PostgreSQL, with CI/CD, IaC, observability, and load testing in place.

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Final improvements that span multiple user stories — documentation, data migration, and quickstart validation.

- [ ] T135 [P] Create data migration script to seed PostgreSQL from existing Derby data (UUID generation for existing integer IDs, date/time format conversion) in scripts/migrate-data.sh
- [ ] T136 [P] Update README.md with modernised architecture overview, service descriptions, build/run instructions, and links to API contracts in README.md
- [ ] T137 [P] Update quickstart.md with final microservices setup instructions — verify a new developer can set up and run all services within 30 minutes (SC-006) in specs/001-modernise-monolith/quickstart.md
- [ ] T138 [P] Add Dependabot configuration for automated dependency vulnerability scanning across all service modules in .github/dependabot.yml
- [ ] T139 Run full end-to-end validation — deploy all services, run contract tests, run load tests, verify coverage thresholds, verify all success criteria (SC-001 through SC-010)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion — BLOCKS all user stories
- **US1 Safety Net (Phase 3)**: Depends on Foundational — BLOCKS all subsequent stories
- **US2 Security (Phase 4)**: Depends on US1 (need tests to verify fixes don't break behaviour)
- **US3 Architecture (Phase 5)**: Depends on US1 + US2 (need tests and secure baseline)
- **US4 Date/Time (Phase 6)**: Depends on US3 (needs clean architecture to avoid reworking deprecated code)
- **US5 Performance (Phase 7)**: Depends on US3 (needs clean DAO layer for pagination). Can run in parallel with US4.
- **US6 Decompose (Phase 8)**: Depends on ALL prior stories (US1–US5 must be complete)
- **Polish (Phase 9)**: Depends on US6

### User Story Dependencies

```
US1 (P1) ──────┐
               ├── US2 (P1) ──── US3 (P2) ──┬── US4 (P3) ──┐
               │                             │              ├── US6 (P4) ── Polish
               │                             └── US5 (P3) ──┘
               └─────────────────────────────────────────────────────────────
```

- **US1 → US2**: Tests must exist before security fixes (characterisation tests catch regressions)
- **US2 → US3**: Security must be fixed before restructuring (don't restructure vulnerable code)
- **US3 → US4, US5**: Architecture must be clean before modernising libraries and performance
- **US4 ∥ US5**: These can run in parallel (different files, no conflicts) — US4 touches entities/utils, US5 touches DAOs/services
- **US4 + US5 → US6**: Both must complete before decomposition (Spring Boot migration needs modern code)

### Within Each User Story

1. Tests MUST be written and FAIL before implementation (Red-Green-Refactor)
2. Models/entities before services
3. Services before controllers/endpoints
4. Core implementation before integration
5. Verification task at the end of each story

---

## Parallel Opportunities

### Within Phase 3 (US1 — Safety Net)

```
# Launch all entity tests in parallel (T008–T011):
T008: UserTest.java              ──┐
T009: CustomerTest.java          ──┤ All write to different files
T010: BillingCategoryTest.java   ──┤
T011: BillableHourTest.java      ──┘

# Launch all DAO tests in parallel (T012–T016):
T012: UserDAOTest.java           ──┐
T013: CustomerDAOTest.java       ──┤ All write to different files
T014: BillingCategoryDAOTest.java──┤
T015: BillableHourDAOTest.java   ──┤
T016: ConnectionManagerTest.java ──┘
```

### Between US4 and US5 (both P3)

```
# US4 and US5 can run in parallel on separate branches:
Developer A (US4):                Developer B (US5):
T068–T070 (date/time tests)       T078–T080 (performance tests)
T071–T075 (entity migration)      T081–T086 (pooling, pagination, N+1)
T076–T077 (build config)          T087–T088 (DTOs, verification)
```

### Within Phase 8 (US6 — Decompose)

```
# After multi-module setup (T089–T092), all services can be built in parallel:
User Service (T097–T101)     ──┐
Customer Service (T102–T106) ──┤ All in separate modules
Billing Service (T107–T112)  ──┤
Reporting Service (T113–T117)──┘

# All Bicep modules can be created in parallel (T124–T130)
# All cross-cutting configs can be applied in parallel (T118–T122)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL — blocks all stories)
3. Complete Phase 3: User Story 1 — Safety Net
4. **STOP and VALIDATE**: Run `./gradlew test jacocoTestReport` — verify 80%+ coverage
5. This is the MVP — the codebase now has a test safety net

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. **US1** → Test safety net (MVP!) — deploy/demo
3. **US2** → Security hardened — deploy/demo
4. **US3** → Clean architecture — deploy/demo
5. **US4 + US5** (parallel) → Modernised and performant — deploy/demo
6. **US6** → Fully decomposed microservices — deploy/demo
7. Polish → Production-ready

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational + US1 together (sequential, shared concern)
2. US2 can be done by one developer while another plans US3
3. US3 requires careful coordination (touches many files)
4. Once US3 is done:
   - Developer A: US4 (date/time modernisation)
   - Developer B: US5 (performance improvements)
5. US6 can be split:
   - Developer A: User Service + Customer Service
   - Developer B: Billing Service + Reporting Service
   - Developer C: Infrastructure (Bicep, CI/CD, Docker)

---

## Notes

- [P] tasks = different files, no dependencies on incomplete tasks in same phase
- [Story] label maps task to specific user story for traceability
- Each user story is independently completable and testable at its checkpoint
- Constitution requires Red-Green-Refactor — verify tests FAIL before implementing
- Commit after each task or logical group using conventional commits: `feat(billing): add invoice endpoint`
- Stop at any checkpoint to validate story independently
- The spec's JSP files are: index.jsp, users.jsp, customers.jsp, categories.jsp, hours.jsp, reports.jsp (6 total)
- Package base: `com.sourcegraph.demo.bigbadmonolith` (monolith phases), service-specific packages in Phase 4

# SC-KPI API Tests

Automated API test framework for the SC-KPI platform.

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Gradle](https://img.shields.io/badge/Gradle-9.3.1-02303A?logo=gradle)
![REST Assured](https://img.shields.io/badge/REST_Assured-6.0.0-4caf50)
![TestNG](https://img.shields.io/badge/TestNG-7.12.0-cd6532)
![Allure](https://img.shields.io/badge/Allure-2.32.0-ff6600?logo=allure)

## Prerequisites

- Java 25+
- SC-KPI API server running (default: `http://localhost:8080`)

## Quick Start

```bash
# Run smoke tests (default)
./gradlew test

# Run all tests
./gradlew test -Dsuite=testng-all.xml
```

## Running Tests

### Test Suites

| Suite | Command | Scope | Parallel | Threads |
|-------|---------|-------|----------|---------|
| Smoke (default) | `./gradlew test` | Health checks + framework validation | methods | 3 |
| Regression | `./gradlew regression` | All domain tests (excl. framework) | classes | 5 |
| All | `./gradlew test -Dsuite=testng-all.xml` | Everything | classes | 5 |

### Custom Task Shortcuts

```bash
./gradlew smoke        # Same as default ./gradlew test
./gradlew regression   # All domain tests, excludes framework group
```

### Group Filtering

```bash
# Include specific groups
./gradlew test -Dsuite=testng-all.xml -DincludeGroups=smoke,positive

# Exclude specific groups
./gradlew test -Dsuite=testng-all.xml -DexcludeGroups=framework
```

Available groups: `smoke`, `regression`, `positive`, `negative`, `schema`, `security`, `method_not_allowed`, `performance`, `contract`, `framework`

## Configuration

### Profiles

Configuration is YAML-based with profile support. Activate a profile with `-Denv=<profile>`.

| Profile | Activation | Base URL | Auth | Parallel | Retries |
|---------|-----------|----------|------|----------|---------|
| default | (none) | `localhost:8080` | disabled | none | 0 |
| ci | `-Denv=ci` | `localhost:8080` | disabled | classes / 5 threads | 1 |
| staging | `-Denv=staging` | `staging-api.sc.kpi.ua` | enabled (tier credentials) | classes / 3 threads | 2 |

### System Property Overrides

Any config value can be overridden via system properties:

```bash
-DbaseUrl=http://localhost:9090
-Dauth.enabled=true
-Dparallel=classes
-DthreadCount=5
-Denv=ci
```

### Authentication

Authentication is **disabled by default** (`auth.enabled=false`).

```bash
# Enable auth
./gradlew test -Dauth.enabled=true

# With staging credentials
./gradlew test -Denv=staging
```

When enabled, auth uses tier-based credentials (BASIC, ADMIN) configured in the active profile YAML. Tokens are cached and refreshed automatically by `AuthManager`.

## Allure Reporting

```bash
# Generate HTML report
./gradlew allureReport
# → build/reports/allure-report/

# Generate and open in browser
./gradlew allureServe
```

Failed tests automatically attach stack traces, response bodies, and exception context to the Allure report.

## Project Structure

```
src/
├── main/java/ua/kpi/sc/test/api/
│   ├── annotation/        # @Authentication, @ApiEndpoint
│   ├── auth/              # AuthContext, AuthContextResolver, AuthManager
│   ├── client/            # ApiClient base + domain clients
│   │   ├── audit/         #   AuditClient
│   │   ├── auth/          #   AuthClient
│   │   ├── club/          #   ClubClient
│   │   ├── department/    #   DepartmentClient
│   │   ├── document/      #   DocumentClient
│   │   ├── notification/  #   NotificationSettingsClient
│   │   ├── project/       #   ProjectClient
│   │   ├── user/          #   UserClient
│   │   └── webhook/       #   TelegramWebhookClient
│   ├── config/            # Config, ConfigurationManager, TestGroup, Endpoint
│   ├── data/              # TestDataFactory, TestDataManager, CleanupRegistry
│   ├── exception/         # Typed exceptions (Api*, Auth*, Cleanup*, Schema*...)
│   ├── listener/          # TestNG listeners (see Architecture)
│   ├── model/             # Request/response DTOs per domain
│   └── util/              # AllureHelper, AssertionHelper, JsonHelper, SchemaValidator
│
└── test/
    ├── java/ua/kpi/sc/test/api/
    │   ├── base/          # Base test classes (see hierarchy below)
    │   └── tests/         # Test classes by domain
    │       ├── audit/
    │       ├── auth/
    │       ├── club/
    │       ├── department/
    │       ├── document/
    │       ├── framework/     # AllureSetupTest, ConfigurationTest
    │       ├── notification/
    │       ├── project/
    │       ├── smoke/         # HealthCheckTest, SwaggerAvailabilityTest
    │       └── user/
    └── resources/
        ├── application.yml          # Default config
        ├── application-ci.yml       # CI profile
        ├── application-staging.yml  # Staging profile
        ├── schemas/                 # JSON Schemas (health, problem-detail)
        └── testng/suites/           # TestNG suite XMLs
```

## Architecture

### Test Class Hierarchy

```
BaseApiTest                       # REST Assured setup, auth resolution, HTTP helpers
├── BasePublicApiTest             # @Authentication(enabled = false)
├── BaseAuthenticatedApiTest      # @Authentication(tier = "BASIC")
├── BaseAdminApiTest              # @Authentication(tier = "ADMIN")
└── BaseSchemaValidationTest      # + JSON Schema validation helpers
```

Each base class applies `@Authentication` at the class level. Individual test methods can override auth via their own `@Authentication` annotation.

### API Client Pattern

```
ApiClient (base)                  # requestSpec(), HTTP methods, connection error handling
├── AuthClient                    # login, register, refresh
├── ClubClient
├── ProjectClient
├── UserClient
├── DocumentClient
├── DepartmentClient
├── AuditClient
├── NotificationSettingsClient
└── TelegramWebhookClient
```

All client methods use `@Step` annotations for Allure traceability. Request/response logging and Allure filters are applied automatically via `requestSpec()`.

### Data Management

```
TestDataFactory                   # Faker-based builders for valid/invalid requests
    ↓
TestDataManager                   # Create entities + register cleanup
    ↓
CleanupRegistry                   # LIFO cleanup execution at suite end
```

- **TestDataFactory** — static factory methods producing randomized DTOs via DataFaker
- **TestDataManager** — orchestrates creation and registers cleanup actions
- **CleanupRegistry** — thread-safe `ConcurrentLinkedDeque`; executed in LIFO order by `ExecutionListener` on suite finish

### Custom Annotations

| Annotation | Target | Purpose |
|-----------|--------|---------|
| `@Authentication(enabled, tier, email, password)` | Class / Method | Declares auth requirements; resolved by `AuthContextResolver` |
| `@ApiEndpoint(path, method, description)` | Class / Method | Endpoint metadata for documentation and Allure labels |

### Listeners

| Listener | Role |
|----------|------|
| `ExecutionListener` | Suite start/finish — logs environment, runs cleanup, clears auth cache |
| `SuiteListener` | Applies parallel config from YAML, logs result summary |
| `AllureTestListener` | Attaches stack traces, response bodies, and thread labels on failure |
| `MethodInterceptor` | Filters test methods by `includeGroups` / `excludeGroups` system properties |
| `RetryListener` | Configurable retry with backoff (`retry.maxAttempts`, `retry.backoffMs`) |

## Tech Stack

| Library | Version | Purpose |
|---------|---------|---------|
| REST Assured | 6.0.0 | HTTP client & response validation |
| TestNG | 7.12.0 | Test framework & parallel execution |
| Allure | 2.32.0 | Test reporting |
| Jackson | 3.0.0 | JSON / YAML serialization |
| AssertJ | 3.27.3 | Fluent assertions |
| DataFaker | 2.5.2 | Test data generation |
| Lombok | 1.18.42 | Boilerplate reduction |
| SLF4J + Logback | 2.0.16 / 1.5.18 | Logging |

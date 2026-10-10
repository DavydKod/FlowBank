# FlowBank

**FlowBank** is a RESTful banking backend built with Java 21, Kotlin, Spring Boot, and PostgreSQL.

The project implements core banking operations with an emphasis on **transactional integrity, concurrency control, database consistency, automated testing, and maintainable backend architecture**.

FlowBank is designed to demonstrate practical backend engineering concepts, including race condition handling, database locking strategies, idempotent operations, integration testing against real PostgreSQL instances, and automated CI pipelines.

## Tech Stack

**Backend**
- Java 21
- Kotlin
- Spring Boot 3
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven

**Database & Infrastructure**
- Flyway
- Docker / Docker Compose
- Testcontainers

**Testing & Quality**
- JUnit 5
- Mockito
- Spring Boot Test
- Maven Surefire & Failsafe
- JaCoCo
- GitHub Actions

## Features

### User Management

- Create, retrieve, update, and delete users
- Unique email validation
- Database-enforced uniqueness constraints
- Optimistic locking for concurrent entity modifications
- Handling of database integrity violations

### Bank Account Management

- Create and retrieve bank accounts
- Associate accounts with users
- Manage account statuses
- Block and unblock accounts
- Maintain account balances
- Enforce non-negative balances
- Configure daily transfer limits
- Validate account ownership and availability

### Banking Operations

FlowBank supports three primary banking operations:

**Deposits**
- Add funds to an account
- Validate account state
- Update balances transactionally

**Withdrawals**
- Withdraw available funds
- Prevent insufficient-funds operations
- Enforce balance constraints

**Transfers**
- Transfer funds between bank accounts
- Validate sender and recipient accounts
- Enforce daily transfer limits
- Apply deterministic pessimistic locking
- Prevent duplicate operations through idempotency keys

### Transaction History & Analytics

- Retrieve transaction history
- Filter transactions by account
- Distinguish incoming and outgoing transactions
- Sort transaction records
- Calculate account transaction statistics
- Provide aggregate values such as totals, averages, and largest transactions

## Concurrency & Data Consistency

Concurrency safety is a central part of FlowBank.

The application combines transactional boundaries, database constraints, and locking strategies to protect financial operations from inconsistent states.

### Transactional Operations

Banking operations execute within Spring-managed database transactions using `@Transactional`.

Operations either complete successfully or roll back when an exception prevents successful execution.

### Pessimistic Locking

Bank account modifications use database-level pessimistic write locks to coordinate concurrent balance updates.

For transfers, accounts are locked in deterministic ID order:

`min(accountId) → max(accountId)`

This reduces the risk of deadlocks when concurrent transfers access overlapping accounts.

### Optimistic Locking

JPA `@Version` fields provide optimistic concurrency control for versioned entities.

Conflicting modifications can be detected rather than silently overwriting changes.

### Database Constraints

Database-level constraints act as a final consistency boundary, including:

- Unique user email addresses
- Unique transaction idempotency keys
- Non-negative account balances
- Referential integrity between related entities

### Race Condition Handling

The application handles integrity violations that can occur when concurrent requests pass preliminary application-level checks.

Spring's `DataIntegrityViolationException` is used where appropriate to translate database constraint violations into meaningful application behavior.

### Idempotency

Financial operations support idempotency keys to prevent duplicate processing.

When a request uses an existing idempotency key:

- **Same operation type and amount:** The previously recorded result is returned without executing the operation again.
- **Different operation type or amount:** A domain exception is thrown to indicate an idempotency conflict.
- **New idempotency key:** The operation is processed normally, subject to business validations.

Database-level uniqueness constraints protect idempotency keys against concurrent duplicate requests.

This ensures that retrying an identical request does not apply the same balance modification multiple times.

### Database Constraints

Database constraints provide an additional layer of data integrity:

- Unique user email addresses
- Unique transaction idempotency keys
- Non-negative account balances
- Referential integrity between users and bank accounts

Application-level validations are complemented by database constraints to handle race conditions that cannot be reliably prevented through preliminary checks alone.

### Code Coverage

JaCoCo is integrated into the Maven build to generate code coverage reports.

Coverage reports provide visibility into tested and untested execution paths, helping evaluate test completeness and identify opportunities for additional testing.

JaCoCo is used for **reporting purposes only**. The build does not enforce minimum coverage thresholds.

### Integration Testing Infrastructure

Integration tests use **Testcontainers with Docker** to run against real PostgreSQL database instances.

This allows persistence, transaction management, database constraints, and concurrency behavior to be tested in an environment closer to production database behavior.

Docker is required for Testcontainers-based tests but is not used to containerize or deploy the FlowBank application.

### Continuous Integration

GitHub Actions provides automated build and test verification.

The CI workflow:

- Builds the application using Maven
- Executes unit tests through Maven Surefire
- Executes integration tests through Maven Failsafe
- Uses Docker and Testcontainers for PostgreSQL integration testing
- Generates JaCoCo code coverage reports

The CI pipeline verifies the automated test suites without enforcing a minimum coverage percentage.

### Requirements

- JDK 21+
- Maven
- PostgreSQL for local application execution
- Docker-compatible container runtime for Testcontainers-based integration tests

### Running Tests

Run unit tests:

```bash
mvn test
```

Run the full Maven verification lifecycle, including integration tests:

```bash
mvn clean verify
```

JaCoCo coverage reports are generated through the configured Maven reporting lifecycle.

### Project Status

**FlowBank v1.0 — Release preparation**

Core banking operations, concurrency control, database consistency mechanisms, unit tests, integration tests, and automated CI verification have been implemented.

The remaining release preparation focuses on documentation and final project review.

FlowBank is an educational and portfolio-oriented backend system, not a production banking service intended for real financial transactions.
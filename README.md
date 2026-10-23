# FlowBank

**Version 2.0.0**

FlowBank is a banking REST API built with Java, Kotlin, Spring Boot, and PostgreSQL. It implements session-based authentication, account management, deposits, withdrawals, transfers, transaction history, and account analytics.

The project focuses on transactional integrity, ownership-based authorization, concurrency control, idempotency, and automated testing against PostgreSQL.

## What's new in 2.0.0

- Registration and login using email and password.
- Argon2 password hashing, with credentials stored separately from user profiles.
- Session authentication, automatic login after registration, and session ID rotation on authentication.
- CSRF protection and a public endpoint for retrieving tokens.
- Logout with session invalidation and deletion of the session cookie.
- Current-user endpoints and ownership checks for accounts, transactions, and analytics.
- Controller integration tests for authentication, sessions, CSRF, and access to other users' resources.
- JaCoCo configuration that collects Surefire coverage alongside integration-test coverage.

## Tech stack

| Area | Technologies |
| --- | --- |
| Languages | Java 25, Kotlin 2.3 |
| Application | Spring Boot 3.5, Spring Web, Bean Validation |
| Security | Spring Security, HTTP sessions, Argon2, CSRF |
| Persistence | Spring Data JPA, Hibernate, PostgreSQL, Flyway |
| API documentation | springdoc-openapi / Swagger UI |
| Testing | JUnit 5, Mockito, MockMvc, Spring Boot Test, Testcontainers |
| Build and CI | Maven, Surefire, Failsafe, JaCoCo, GitHub Actions |

## Features and business rules

### Users and authentication

- Register with a name, unique email, and password.
- Registration passwords must contain 17–128 characters; names must contain 3–50 characters.
- Authenticate against stored password hashes.
- Retrieve the current user's profile, change their name, and delete their profile.
- Delete a user only when they have no bank accounts. Successful deletion also logs out the current session.
- Return profiles without exposing passwords or password hashes.

### Bank accounts and money operations

- Create accounts for the authenticated user and list their accounts with pagination.
- Retrieve, block, unblock, or close an owned account.
- New accounts start active with a zero balance and an outgoing limit of 1,000.00.
- Deposit into and withdraw from owned accounts.
- Transfer from an owned account to a different account, including another user's account.
- Require active accounts for balance changes and a zero balance before closing an account.
- Configure an active account's outgoing limit.
- Reject insufficient funds and operations exceeding the outgoing limit.

The outgoing limit applies to a **rolling 24-hour window**, rather than a calendar day. Withdrawals and outgoing transfers count toward it; deposits do not.

Money uses `BigDecimal` in application code and `NUMERIC(19, 2)` in PostgreSQL. Operation amounts must be positive, with at most 17 integer digits and two decimal places.

### History and analytics

- Retrieve transaction details when the current user owns either participating account.
- Retrieve an owned account's history with pagination, incoming/outgoing filtering, and sorting.
- Calculate sent/received totals, counts, averages, and largest transaction amounts.
- Read analytics within a `REPEATABLE_READ` transaction so the aggregate queries use a consistent database snapshot.

## Authentication and authorization

FlowBank uses the `JSESSIONID` session cookie. Registration and login save authentication in the HTTP session; subsequent requests must retain that cookie.

The public application endpoints are `GET /auth/csrf`, `POST /auth/register`, and `POST /auth/login`. Registration and login still require a valid CSRF token. The security filter permits logout, which also requires CSRF protection.

Other application endpoints require authentication:

- Account operations and analytics require account ownership.
- Transfers require ownership of the source account.
- Transaction details are visible to the owner of either participating account.
- Current-user routes derive the user ID from the authenticated principal.

Access to another user's protected resource returns **404**, matching a missing resource, to avoid disclosing its existence. Missing authentication returns **401**. Missing or invalid CSRF tokens on state-changing requests return **403**.

### Session and CSRF workflow

1. Call `GET /auth/csrf` and retain the session cookie.
2. Read `token`, `headerName`, and `parameterName` from the response.
3. Send the token in the indicated header, together with the cookie, when registering or logging in.
4. After successful registration or login, retain the updated session cookie and **fetch a new CSRF token**. Authentication rotates an existing session ID and clears the previous CSRF token.
5. Include the cookie on authenticated requests and the current CSRF token on `POST`, `PATCH`, `PUT`, and `DELETE` requests. `GET` requests do not require a CSRF token.
6. Log out with `POST /auth/logout`, using the cookie and current CSRF token.

Example registration body:

```json
{
  "name": "Davyd",
  "email": "davyd@example.com",
  "password": "a-long-example-password"
}
```

## API overview

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/auth/csrf` | Obtain a CSRF token |
| POST | `/auth/register` | Register and authenticate |
| POST | `/auth/login` | Authenticate an existing user |
| POST | `/auth/logout` | Invalidate the session and delete its cookie |
| GET | `/auth/me` | Retrieve the current profile |
| PATCH | `/users/me/name` | Change the current user's name |
| DELETE | `/users/me` | Delete the current user, if they have no accounts |
| POST | `/accounts` | Create an account for the current user |
| GET | `/accounts/me` | List the current user's accounts |
| GET | `/accounts/{id}` | Retrieve an owned account |
| PATCH | `/accounts/{id}/block` | Block an owned account |
| PATCH | `/accounts/{id}/unblock` | Unblock an owned account |
| PATCH | `/accounts/{id}/close` | Close an owned account with a zero balance |
| PATCH | `/accounts/{id}/dailyOutgoingLimit` | Change the outgoing limit |
| POST | `/transactions/deposit` | Deposit funds |
| POST | `/transactions/withdraw` | Withdraw funds |
| POST | `/transactions/transfer` | Transfer funds |
| GET | `/transactions/{id}` | Retrieve a transaction involving an owned account |
| GET | `/transactions/by-account/{accountId}` | Retrieve an owned account's history |
| GET | `/analytics/accounts/{id}` | Retrieve an owned account's analytics |

Deposit and withdrawal bodies use `accountId` and `amount`. Transfer bodies use `fromAccountId`, `toAccountId`, and `amount`. Changing the outgoing limit uses an `amount` field.

Swagger UI is available at `/swagger-ui/index.html`; the OpenAPI document is at `/v3/api-docs`. The current security configuration requires authentication for these routes too; access them using a client that retains an authenticated session.

## Transactions, concurrency, and idempotency

### Data consistency

- Balance changes and transaction records execute within the same Spring-managed database transaction.
- Balance-changing operations acquire pessimistic write locks on the affected accounts.
- Transfers lock accounts in ascending ID order to reduce deadlock risk.
- JPA `@Version` fields detect conflicting updates to users and accounts.
- Database constraints enforce unique emails and idempotency keys, non-negative balances, positive transaction amounts, and referential integrity.
- Constraint violations are translated into application errors where appropriate.

### Idempotency contract

Deposits, withdrawals, and transfers require an `Idempotency-Key` header. Keys are nonblank, at most 100 characters long, and globally unique across transaction records.

For an authorized request with an existing key:

- Matching source account, destination account, and amount return the stored transaction without applying another balance change.
- Different account IDs or amount produce **409 Conflict**.
- Deposits and withdrawals use a null source or destination respectively, distinguishing them from transfers.

The service checks the key before acquiring account locks and again after locking. The preliminary check avoids unnecessary locks for repeated requests; the second accounts for changes committed while waiting for a lock.

A database uniqueness constraint provides the final safeguard, including races between requests that lock different accounts. If a key collision reaches `saveAndFlush`, the operation rolls back and returns **409 Conflict**, rather than returning the existing transaction from that exception path.

## Running locally

### Requirements

- JDK 25.
- Maven available on `PATH`.
- A running PostgreSQL instance for the application.
- A running Docker-compatible container runtime for integration tests. Testcontainers uses PostgreSQL 17.

### Setup

1. Create a PostgreSQL database:

   ```sql
   CREATE DATABASE flow_bank;
   ```

2. Copy `src/main/resources/application.properties.example` to `src/main/resources/application.properties` and configure the connection:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/flow_bank
   spring.datasource.username=YOUR_USERNAME
   spring.datasource.password=YOUR_PASSWORD
   ```

   The local configuration file is ignored by Git. The example also sets Hibernate schema validation and pagination defaults. Flyway applies database migrations during startup.

3. Start the application:

   ```bash
   mvn spring-boot:run
   ```

The default base URL is `http://localhost:8080`. Follow the session and CSRF workflow above before calling authenticated endpoints.

## Tests and coverage

Unit tests cover domain rules and service behavior. Integration tests exercise repositories, services, concurrency scenarios, and controller requests against PostgreSQL through Testcontainers.

Controller tests use MockMvc with the Spring application context and security filters. Authentication integration tests use real services and password verification to check registration, login, logout, session isolation, session ID rotation, and CSRF behavior.

Run unit tests:

```bash
mvn clean test
```

Run the complete suite and generate combined coverage:

```bash
mvn clean verify
```

Run integration tests without unit tests:

```bash
mvn clean verify -DskipUnitTests=true
```

Run only the authentication controller integration tests:

```bash
mvn verify -Dit.test=AuthControllerIT -DskipUnitTests=true
```

Generate coverage from unit tests only:

```bash
mvn clean test jacoco:report
```

Surefire runs unit tests; Failsafe runs `*IT` classes. JaCoCo collects data from both during a full `verify` run. Reports are written to:

- Coverage: `target/site/jacoco/index.html`.
- Unit-test results: `target/surefire-reports/`.
- Integration-test results: `target/failsafe-reports/`.

JaCoCo reports coverage without enforcing a minimum threshold. Integration tests start their own PostgreSQL container and do not require the local application database.

## Continuous integration

GitHub Actions runs on pull requests targeting `main`, using Java 25 and separate jobs for:

- Unit tests: `mvn clean test`.
- Integration tests: `mvn clean verify -DskipUnitTests=true`.

The integration job generates a JaCoCo report for the tests it runs. A local `mvn clean verify` runs both suites together and produces their combined coverage.

## Project structure

The application is organized by feature under `src/main/java/com/davyd`:

- `auth`: registration, login, session endpoints, and credentials.
- `security`: security configuration, principals, and ownership validation.
- `user`: user profiles and persistence.
- `bankaccount`: account lifecycle, balances, and outgoing limits.
- `transaction`: money operations, history, and idempotency.
- `analytics`: Kotlin account analytics.
- `common`: shared validation, configuration, and exception handling.

SQL migrations are in `src/main/resources/db/migration`. Tests are grouped into `unit`, `integration`, and `support` under `src/test/java/com/davyd`.

## Scope

FlowBank is an educational and portfolio backend. Deposits and withdrawals model internal operations; the application does not integrate with an external payment provider or process real funds.

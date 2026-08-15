# FlowBank

FlowBank is a backend banking system built with **Java and Spring Boot**.

The project is designed to model core banking operations while focusing on backend architecture, data consistency, transaction management, concurrency control, testing, and production-oriented development practices.

## Tech Stack

* Java
* Spring Boot
* Spring Data JPA
* Hibernate
* PostgreSQL
* Maven
* JUnit 5
* Mockito

## Features

### User Management

* Create users
* Retrieve users
* Update users
* Delete users
* Unique email validation

### Bank Account Management

* Create bank accounts
* Retrieve accounts
* Account ownership
* Account statuses
* Block and unblock accounts
* Balance management

### Transactions

* Transfer money between accounts
* Transaction history
* Filter transactions by account
* Incoming and outgoing transactions
* Transaction sorting
* Balance validation
* Account status validation

### Concurrency & Data Consistency

* Transactional money transfers with `@Transactional`
* Pessimistic locking for bank accounts
* Deterministic lock ordering to prevent deadlocks
* Optimistic locking with `@Version`

### Error Handling

* Global exception handling with `@RestControllerAdvice`
* Custom domain exceptions
* Structured API error responses

### Testing

* Unit testing with JUnit 5
* Dependency mocking with Mockito

## Architecture

The application follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

Main layers:

```text
controller/
dto/
service/
repository/
model/
exception/
```

Controllers handle HTTP requests, services contain business logic, repositories provide database access, and entities represent the persistence model.

## Transaction Safety

Money transfers are executed inside database transactions.

Both participating bank accounts are locked using pessimistic write locks before their balances are modified.

Locks are acquired in deterministic ID order:

```text
min(accountId) → max(accountId)
```

This reduces the risk of database deadlocks when multiple transfers involving the same accounts are executed concurrently.

## API

The REST API currently provides endpoints for:

```text
/users
/accounts
/transactions
```

The API is actively evolving as new banking functionality is implemented.

## Planned Features

* Authentication and authorization
* Spring Security
* JWT authentication
* Roles and permissions
* Docker / Docker Compose
* Integration tests
* CI/CD with GitHub Actions
* Transaction pagination
* Improved API documentation
* Auditing
* Database migrations
* Additional banking operations

## Running the Project

### Requirements

* Java
* Maven
* PostgreSQL

Clone the repository:

```bash
git clone <repository-url>
cd FlowBank
```

Configure the PostgreSQL connection in your application configuration.

Run the application:

```bash
mvn spring-boot:run
```

Run tests:

```bash
mvn test
```

## Project Status

🚧 **In active development**

FlowBank is being continuously expanded with new banking functionality, infrastructure, testing, security, and production-oriented features.

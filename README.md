# Midas
Project repo for the JPMC Advanced Software Engineering Forage program
> A financial transaction processing backend built with Spring Boot, Apache Kafka, JPA, and REST APIs.

---
## Overview

**Midas Core** is the central processing service in the Midas system. It:

- 📨 **Receives** financial transactions asynchronously via Apache Kafka
- ✅ **Validates** transactions against business rules (user existence, sufficient balance)
- 💾 **Persists** valid transactions to an H2 SQL database using JPA
- 💰 **Enriches** transactions with incentive data from an external REST API
- 🌐 **Exposes** user balances through a REST API endpoint

```
Kafka Topic (trader-updates)
        │
        ▼
KafkaTransactionListener
        │
        ▼
DatabaseConduit (Validate → Incentive → Persist)
        │                        │
        ▼                        ▼
  H2 Database          Incentive API (port 8080)
        │
        ▼
  GET /balance (port 33400)
```

---

## Architecture

Midas Core follows a **4-layer architecture**:

| Layer | Class | Responsibility |
|---|---|---|
| **Transport** | `KafkaTransactionListener` | Consumes Kafka messages, receives HTTP requests |
| **Service** | `DatabaseConduit` | Business logic, validation, orchestration |
| **Repository** | `UserRepository`, `TransactionRepository` | Data access abstraction |
| **Database** | H2 (dev) | Persistence |

### Why this design?

- **Kafka decoupling** — the transaction producer never talks directly to Midas Core. If Midas Core is slow or restarted, messages queue safely in Kafka with no data loss.
- **JPA abstraction** — swapping H2 for PostgreSQL in production requires only a config change, no Java code changes.
- **REST as a contract** — the Incentive API is consumed via HTTP. Neither team needs to know the other's internal implementation.

---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| **Java** | 17 (LTS) | Language |
| **Spring Boot** | 3.2.5 | Application framework + auto-configuration |
| **Spring Kafka** | 3.1.4 | Kafka consumer/producer integration |
| **Spring Data JPA** | 3.2.5 | ORM and database abstraction |
| **Hibernate** | (via JPA) | JPA implementation |
| **H2 Database** | 2.2.224 | In-memory SQL database |
| **Jackson** | (via Spring) | JSON serialization/deserialization |
| **JUnit 5** | (via Spring Test) | Testing framework |
| **Testcontainers / Embedded Kafka** | 1.19.1 | Integration test infrastructure |
| **Maven** | 3.x | Build tool and dependency management |

---



## Getting Started

### Prerequisites

- **Java 17** — [Download from Oracle](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- **Maven 3.x** — [Download](https://maven.apache.org/download.cgi)
- **Git**

### 1. Fork & Clone

```bash
# Fork the repo on GitHub first, then:
git clone https://github.com/YOUR-USERNAME/forage-midas.git
cd forage-midas
```

### 2. Set JAVA_HOME (Windows)

```
Variable Name:  JAVA_HOME
Variable Value: C:\Program Files\Java\jdk-17
Add to PATH:    C:\Program Files\Java\jdk-17\bin
```

Verify:
```bash
java -version   # should show 17.x.x
mvn -version    # should show Maven 3.x
```

### 3. Build the Project

```bash
mvn clean install
```

### 4. Start the Incentive API *(required for Tasks 4 & 5)*

```bash
java -jar services/transaction-incentive-api.jar
# Runs on http://localhost:8080
```

### 5. Run Midas Core

```bash
mvn spring-boot:run
# Runs on http://localhost:33400
```

## Task Breakdown

###  Task 1 — Project Setup
- Added all Maven dependencies to `pom.xml` (Spring Boot, Kafka, H2, JPA, Test)
- Configured `application.yml` with Kafka topic and server port
- Verified build: `mvn clean install` → `mvn spring-boot:run`

---

###  Task 2 — Kafka Listener

Implemented `KafkaTransactionListener` — a `@Component` that listens to the configured Kafka topic and deserializes each message into a `Transaction` object.
```java
@KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core-group")
public void listen(Transaction transaction) {
    // Process each incoming transaction
}
```
Also implemented `KafkaConsumerConfig` to configure the `ConsumerFactory` with `JsonDeserializer<Transaction>` and provide a `KafkaTemplate` bean for test producers.

---

### Task 3 — Database Integration

Integrated H2 + Spring Data JPA:
- Created `TransactionRecord` — a `@Entity` with `@ManyToOne` relationships to `UserRecord` (sender + recipient)
- Created `DatabaseConduit` with `@Transactional` `processTransaction()` that validates and persists transactions

**Validation rules:**
```
senderId         must exist in DB
recipientId      must exist in DB
sender.balance   must be >= transaction.amount
```

If valid → deduct from sender, credit recipient, save `TransactionRecord`.  
If invalid → discard silently, no DB changes made.


---

###  Task 4 — Incentive API Integration
Extended `DatabaseConduit` to call the external Incentive API for each valid transaction using `RestTemplate`.
---

###  Task 5 — REST API Endpoint
Created `BalanceController` exposing `GET /balance`.The API controller exposes a “/balance” endpoint that responds exclusively to GET requests, accepts a userId as a request parameter, and returns an instance of the provided Balance class serialized to JSON. 
Integrated the REST Controller directly into Midas Core.

## Running Tests

```bash
# Run all tests
mvn test

# Run a specific task's tests
mvn -Dtest=TaskOneTests test
mvn -Dtest=TaskTwoTests test
mvn -Dtest=TaskThreeTests test
mvn -Dtest=TaskFourTests test
mvn -Dtest=TaskFiveTests test
```
---

## Key Concepts
### Why Kafka?
Kafka decouples the transaction producer from Midas Core. The producer publishes and moves on — Midas Core consumes at its own pace. If Midas Core restarts, it resumes from its last committed offset with **zero message loss**.

### Why SQL (H2) over NoSQL?
Financial data demands **ACID guarantees**. Atomicity ensures money never disappears between accounts. SQL databases provide this; NoSQL databases trade it for speed. H2 is used here for development convenience — the JPA abstraction makes swapping to PostgreSQL in production a **config-only change**.

###  Why @Transactional?
A transaction that debits the sender and credits the recipient must be **atomic** — either both happen or neither does. `@Transactional` wraps the entire method in a DB transaction. If anything throws, all changes roll back automatically.

###  Why Constructor Injection?
Dependencies declared in the constructor can be `final` (immutable), are impossible to forget (won't compile without them), and make the class **trivially testable** by passing mock objects in unit tests.

###  REST as a Contract
The Incentive API and Midas Core are owned by different teams. The REST API is their contract — as long as the endpoint and response shape don't change, either service can evolve independently. `@JsonIgnoreProperties(ignoreUnknown = true)` on `Incentive.java` future-proofs Midas Core against the API adding new fields.

---

## What I Learned

| Topic | Key Takeaway |
|---|---|
| **Apache Kafka** | Message queues decouple services and enable resilient async communication at scale |
| **Spring Boot Auto-config** | Adding a JAR to the classpath is enough for Spring to configure and wire it |
| **JPA / Hibernate** | ORM maps Java objects to DB tables — you write Java, Hibernate writes SQL |
| **@Transactional** | ACID atomicity in one annotation — all DB ops commit or roll back together |
| **REST API design** | The API is a contract between teams — breaking it breaks consumers |
| **Dependency Injection** | Constructor injection = immutable, testable, explicit dependencies |
| **Embedded Kafka in tests** | Real-behaviour integration tests without needing a running broker |
| **Architecture trade-offs** | Good design balances cleanliness against deployment burden and development time |

---

## Author

Built as part of the **JPMorgan Chase & Co. Forage Virtual Experience**.

[![Forage](https://img.shields.io/badge/JPMorgan-Forage-blue?style=flat-square)](https://www.theforage.com/)
[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-3.1.4-black?style=flat-square&logo=apachekafka)](https://kafka.apache.org/)
[![Maven](https://img.shields.io/badge/Maven-build-red?style=flat-square&logo=apachemaven)](https://maven.apache.org/)

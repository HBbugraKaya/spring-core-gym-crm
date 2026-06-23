# Spring Core Gym CRM

A Spring Core based Gym CRM module that uses separate in-memory Map storage beans for trainees, trainers, and trainings.

## Technology Stack

- Java 21
- Maven Wrapper 3.9.9
- Spring Framework 6.2.x
- JUnit 5, Mockito, AssertJ, and JaCoCo

## Build and Test

Windows:

```powershell
.\mvnw.cmd clean verify
```

Linux/macOS:

```bash
./mvnw clean verify
```

The JaCoCo line coverage threshold is 80%. The current test suite has 43 tests and 97.32% line coverage. The HTML coverage report is generated at `target/site/jacoco/index.html` after the build.

## Run

```powershell
.\mvnw.cmd exec:java
```

To run the demo scenario:

```powershell
.\mvnw.cmd exec:java "-Dexec.args=--demo"
```

The default property file is `config/application.properties`, and the initial data file is `config/initial-data.json`. To use a different property file, pass the JVM system property:

```powershell
.\mvnw.cmd -Dgym.config.path=C:\path\application.properties exec:java
```

## Implementation Notes

- Spring context is configured with annotation-based and Java-based configuration.
- Each storage is a separate Spring bean backed by `ConcurrentHashMap`.
- Initial data is loaded from a file during application startup using a `BeanPostProcessor`.
- DAO dependencies are injected through setter injection.
- Service beans are injected into `GymFacade` through constructor injection.
- Trainee and trainer usernames are generated from first name and last name, with suffixes added for duplicate name pairs.
- Passwords are generated as random 10-character alphanumeric strings.
- Logging avoids sensitive data such as passwords, addresses, and dates of birth.

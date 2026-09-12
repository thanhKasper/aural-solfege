# Aural Solfege

A Spring Boot backend application for interval-based ear training. It generates musical interval audio (WAV) via MIDI synthesis and provides a REST API for managing ear training exercises and practice sessions.

## Features

- **Musical Interval Audio Generation** - Generates WAV audio files of musical intervals (e.g., MAJOR_2ND, PERFECT_5TH) using SoundFont2 or harmonic oscillator synthesis
- **Ear Training Exercises** - Define exercises with activities such as single interval listening and interval sound comparison
- **Practice Sessions** - Step-by-step session state machine with tracking (PENDING → ACTIVE → COMPLETED → SKIPPED)
- **Paged Exercise Retrieval** - Query exercises with pagination support
- **Polymorphic DTO Serialization** - Dynamic exercise activity types via Jackson polymorphism

## Tech Stack

| Component         | Technology                                      |
| ----------------- | ----------------------------------------------- |
| Language          | Java 25                                         |
| Framework         | Spring Boot 4.1.1 (Web MVC, Data JPA, Validation, gRPC) |
| Build Tool        | Maven                                           |
| Database          | PostgreSQL 16                                   |
| Object Mapping    | Lombok + Jackson 3 (with mixins & polymorphism) |
| Audio Engine      | Java Sound API (MIDI → WAV pipeline)            |
| ORM               | Hibernate (JPA)                                 |

## Prerequisites

- **Java 25** (runtime JVM flag `--add-exports=java.desktop/com.sun.media.sound=ALL-UNNAMED`; not needed for compilation)
- **Docker** (for PostgreSQL via Docker Compose)
- **Maven 3.x**
- A **SoundFont2 (.sf2) file** placed at `src/main/resources/soundfonts/grand_piano.sf2` (~266 MB, gitignored)

## Getting Started

### 1. Start the database

```bash
docker compose up -d
```

This runs PostgreSQL 16 on port **5432** with:
- Database: `solfege-db`
- User: `solfege`
- Password: `123456`

### 2. Place the SoundFont

Download a General MIDI SoundFont (e.g., `GrandPiano.sf2`) and place it at:

```
src/main/resources/soundfonts/grand_piano.sf2
```

### 3. Build and run

```bash
mvn clean package
mvn spring-boot:run
```

Or run `vn.ktt.AuralSolfegeApplication` from your IDE (IntelliJ IDEA recommended). When running from an IDE, add the VM option `--add-exports=java.desktop/com.sun.media.sound=ALL-UNNAMED` to the run configuration.

The application starts on **http://localhost:8080** by default.

> **Note:** The database schema is recreated on each startup (`ddl-auto=create`) and seeded with initial data from `import.sql`.

## Configuration

Key properties in `src/main/resources/application.properties`:

| Property                      | Default                                      | Description                          |
| ----------------------------- | -------------------------------------------- | ------------------------------------ |
| `spring.datasource.url`       | `jdbc:postgresql://localhost:5432/solfege-db` | PostgreSQL connection URL            |
| `spring.datasource.username`  | `solfege`                                    | Database username                    |
| `spring.datasource.password`  | `123456`                                     | Database password                    |
| `spring.jpa.hibernate.ddl-auto` | `create`                                   | Schema generation strategy           |
| `soundfont.path`              | `classpath:soundfonts/grand_piano.sf2`       | Path to the SoundFont2 file          |

## API Reference

### Sound Generation

| Method | Endpoint                                        | Description                                          |
| ------ | ----------------------------------------------- | ---------------------------------------------------- |
| GET    | `/api/intervals/{interval}/random?texture=`     | Generate a random single interval as WAV download    |
| GET    | `/api/interval-range/{interval}?texture=&direction=` | Generate interval range sweep as WAV download  |

**Parameters:**
- `{interval}` - Interval notation (e.g., `M2`, `P5`)
- `texture` - `ASCENDING` | `DESCENDING` | `STACKED`
- `direction` - `UP` | `DOWN` (range endpoint only)

### Exercises

| Method | Endpoint                       | Description                          |
| ------ | ------------------------------ | ------------------------------------ |
| GET    | `/api/exercises?page=&pageSize=` | List exercises (paginated)         |
| GET    | `/api/exercises/{id}`          | Get exercise by ID                   |
| POST   | `/api/exercises`               | Create a new exercise                |
| POST   | `/api/exercises/{id}/sessions` | Start a practice session             |

**Exercise Activity Types:**
- `SINGLE_INTERVAL` - Listen to a single interval
- `INTERVAL_SOUND_COMPARISON` - Compare two intervals
- `COOL_DOWN` - Rest period between activities

### Practice Sessions

| Method | Endpoint                         | Description                              |
| ------ | -------------------------------- | ---------------------------------------- |
| POST   | `/api/sessions/{id}/advance`     | Complete current step and advance        |
| POST   | `/api/sessions/{id}/conclude`    | End session and get results              |

## Architecture

The project follows **Clean/Hexagonal Architecture** with two bounded contexts:

```
music/                                  eartraining/
├── domain/                             ├── domain/
├── application/                        │   ├── exercise/  (root aggregate)
└── infrastructure/                     │   ├── session/   (root aggregate)
                                        │   └── guard/
                                        ├── application/   (shared ports, use-cases)
                                        └── infrastructure/(shared REST, JPA)
```

Each bounded context is organized into:
- **domain** - Entities, value objects, enums, repository interfaces
- **application** - Use cases, ports (inbound/outbound), DTOs, mappers
- **infrastructure** - Controllers, JPA entities/adapters, Spring configuration

**Bounded contexts:**
- `music` - Music domain, sound generation, and audio output
- `eartraining` - Ear-training domain logic; its `domain` layer contains the two root aggregates `Exercise` and `PracticeSession` (each with its own entity, value objects, and repository), sharing the `application` and `infrastructure` layers above them

**Planned evolution:** `music` and `eartraining` are designed to be split into **two independent services** in the future to improve maintainability - a service for musical/audio processing and a service for the ear-training domain logic.

**Key patterns:**
- Dependency inversion via interfaces (`IExerciseRepository`, `ISoundGeneratorPort`, etc.)
- Shared abstractions (`ServiceRegistry`, `DataMapperRegistry`)
- Jackson polymorphic serialization for exercise activities and practice steps
- State machine for practice steps (PENDING → ACTIVE → COMPLETED → SKIPPED)

## Project Structure

```
aural-solfege/
├── pom.xml
├── docker-compose.yml
├── src/
│   ├── main/
│   │   ├── java/vn/ktt/
│   │   │   ├── AuralSolfegeApplication.java
│   │   │   ├── shared/                           # Generic registries & mappers
│   │   │   ├── music/                            # Music domain & sound generation
│   │   │   │   ├── domain/                       # atom, composition, instrument, factory, service
│   │   │   │   ├── application/                  # sound, instrument (ports + use cases)
│   │   │   │   └── infrastructure/               # controller, grpc, audio, persistence, config
│   │   │   └── eartraining/                      # Ear-training domain logic
│   │   │       ├── domain/
│   │   │       │   ├── exercise/                 # Root aggregate: exercises
│   │   │       │   ├── session/                  # Root aggregate: practice sessions
│   │   │       │   └── guard/
│   │   │       ├── application/                  # dto, mapper, inbound/outbound ports, use cases
│   │   │       └── infrastructure/               # controller, persistence, jackson, grpc, config
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── import.sql                        # Seed data
│   │       └── soundfonts/
│   └── test/                                     # Mapper-registry unit tests (41)
└── target/
```

## Seed Data

On startup, the following data is inserted via `import.sql`:

- **Exercises:** "Basic Interval Training" (single intervals), "Interval Sound Comparison" (comparing intervals)
- **Instrument:** PIANO (range A0–C8)
- **Musical Config:** Active instrument set to PIANO

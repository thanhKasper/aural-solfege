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
| Language          | Java 26                                         |
| Framework         | Spring Boot 3.5.8 (Web MVC, Data JPA, Validation) |
| Build Tool        | Maven                                           |
| Database          | PostgreSQL 16                                   |
| Object Mapping    | Lombok + Jackson (with mixins & polymorphism)   |
| Audio Engine      | Java Sound API (MIDI → WAV pipeline)            |
| ORM               | Hibernate (JPA)                                 |

## Prerequisites

- **Java 26** (JDK with `--add-exports=java.desktop/com.sun.media.sound=ALL-UNNAMED`)
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

Or run `vn.ktt.Main` from your IDE (IntelliJ IDEA recommended).

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
musical_components_core/          ear_training_system/
├── musical_domains/              ├── domain/
├── musical_application/          │   ├── exercise/
└── musical_infrastructure/       │   └── practice_session/
                                  ├── application/
                                  └── infrastructure/
```

Each context is organized into:
- **domain** - Entities, value objects, enums, repository interfaces
- **application** - Use cases, ports (inbound/outbound), DTOs, mappers
- **infrastructure** - Controllers, JPA entities/adapters, Spring configuration

**Key patterns:**
- Dependency inversion via interfaces (`IExerciseRepository`, `SoundGeneratorPort`, etc.)
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
│   │   │   ├── Main.java
│   │   │   ├── shared/                           # Generic registries & mappers
│   │   │   ├── musical_components_core/          # Music domain & sound generation
│   │   │   │   ├── musical_domains/
│   │   │   │   ├── musical_application/
│   │   │   │   └── musical_infrastructure/
│   │   │   └── ear_training_system/              # Exercise & session management
│   │   │       ├── domain/
│   │   │       ├── application/
│   │   │       └── infrastructure/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── import.sql                        # Seed data
│   │       └── soundfonts/
│   └── test/                                     # (empty)
└── target/
```

## Seed Data

On startup, the following data is inserted via `import.sql`:

- **Exercises:** "Basic Interval Training" (single intervals), "Interval Sound Comparison" (comparing intervals)
- **Instrument:** PIANO (range A0–C8)
- **Musical Config:** Active instrument set to PIANO

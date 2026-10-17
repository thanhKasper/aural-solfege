# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

Requires JDK 25 (enforced by `maven-enforcer-plugin`). There is no Maven wrapper; use a system `mvn`.

```bash
docker compose up -d                 # PostgreSQL 16 on :5432 (solfege-db / solfege / 123456)
mvn clean package                    # build + tests; also generates gRPC stubs from src/main/proto
mvn spring-boot:run                  # HTTP :8080, gRPC server :9090
mvn test                             # no DB needed (unit tests + one @JsonTest slice)
mvn test -Dtest=PersistedJsonCompatibilityTest               # single class
mvn test -Dtest=PersistedJsonCompatibilityTest#seededSingleIntervalExerciseStillDeserializes
```

- The runtime JVM needs `--add-exports=java.desktop/com.sun.media.sound=ALL-UNNAMED`. `pom.xml` already sets it for `spring-boot:run`. Add it by hand to IDE run configs.
- `src/main/resources/soundfonts/grand_piano.sf2` (~266 MB, gitignored) must exist for `Sf2BasedMidiRenderer`, which is `@Primary`.
- Generated gRPC classes (`vn.ktt.grpc.sound.*`) live in `target/`. If they're "not found", run `mvn compile`.
- `ddl-auto=create`: the schema is dropped and re-seeded from `import.sql` on every startup.

## Architecture

Hexagonal architecture with two bounded contexts under `vn.ktt`, each split into `domain` / `application` / `infrastructure`. They are planned to become **two separate services**, so keep them decoupled:

- `music`: pitches, intervals, and chords (`MusicalEntityFactory` parses notation like `M2`/`P5`), plus audio generation: MIDI `Sequence` → `IMidiRenderer` (SF2, or the `HarmonicMidiRenderer` oscillator) → PCM → `WavEncoder`.
- `eartraining`: two aggregates. `Exercise` holds polymorphic `ExerciseActivity`s and a repetition count. `PracticeSession` holds `PracticeStep`s. Sessions and steps are status state machines that throw `IllegalStateException` on invalid transitions.
- **Cross-context calls go over gRPC, even though both contexts run in one process.** `eartraining` defines the outbound port `IIntervalComparisonPort`. `GrpcIntervalComparisonClient` implements it and calls `SoundGrpcService` on `localhost:9090` (contract: `src/main/proto/sound_service.proto`). The call happens inside `IntervalSoundComparisonStepContextMapper`, when a step is mapped to its DTO. So a gRPC failure shows up as an error from the session endpoints. The client maps `MusicalInterval` to notation with a `switch`, so that switch must be updated whenever the enum changes.
- Known coupling leak: the `IMusicalEntityFactory` bean is declared in `eartraining`'s `DomainServiceConfig`, but only `music` classes use it. It belongs in `MusicalDomainServiceConfig`.
- Domain classes have no Spring annotations. Domain services and guards are wired as `@Bean`s in `infrastructure/config`. `application/*UseCase` implements `application/inbound/I*Port`. Domain repository interfaces are implemented by `infrastructure/persistence/gateway/*Repository`, which wraps Spring Data `*JpaRepository`.
- Error handling and transactions are not implemented yet (no `@ControllerAdvice`, no `@Transactional`), so domain `IllegalArgumentException`/`IllegalStateException`s currently reach the client as a 500. Until that is fixed:
  - Don't add per-controller `try/catch` or ad-hoc status mapping. The planned fix is one `@RestControllerAdvice` per context in `infrastructure/controller` (`IllegalArgumentException` → 400, `IllegalStateException` → 409).
  - Keep throwing plain `IllegalArgumentException`/`IllegalStateException` from the domain. Don't put Spring exceptions or HTTP concerns there.
  - If a use case writes to more than one repository, call it out: without `@Transactional` the writes are not atomic.

### Model layers and type-keyed registries

Every polymorphic concept exists in three parallel forms: a domain value object, an application DTO, and a persistence entity. Examples: `SingleIntervalExerciseActivity` / `…DTO` / `…Entity`, and `ListenIntervalContext` / `ListenIntervalStepDTO` / `ListenIntervalContextEntity`. Nothing uses `instanceof` or `switch` to dispatch on these types. Dispatch goes through the generic registries in `vn.ktt.shared`, which collect beans by type:

- `DataMapperRegistry<Key, From, To>` indexes `IDataMapper` beans by source and target class. It resolves the one mapper that matches an object's runtime class, and throws on duplicate keys, no match, or ambiguous matches. Its subclasses are the four `*MapperFactory` classes: activity and step-context, each for DTO↔domain and entity↔domain.
- `ServiceRegistry<Key, Service>` looks up a service by the key's exact runtime class. `StepGenerationService` uses it to pick an `IStepGeneration` for each `ExerciseActivity` subtype, then expands the activities into `StepDefinition`s × repetitions.

### Polymorphic JSON (two independent mechanisms)

- **API DTOs** (`ExerciseActivityDTO`, `PracticeStepDTO`): `JacksonConfiguration` applies a `@JsonTypeInfo(property = "type")` mixin and registers subtypes from `I*DTOProvider` beans. Type names come from `ExerciseActivityType` / `PracticeStepType`.
- **Persistence**: `exercises.exercise_activities` and `practice_steps.context` are JSON strings, written by the JPA `AttributeConverter`s in `persistence/converter`. The converters use their own static `JsonMapper` with hard-coded `@JsonSubTypes` on `ExerciseActivityEntity` / `StepContextEntity`. Spring's mapper is not involved.
- This is Jackson 3 (`tools.jackson.*`), but the annotations still come from `com.fasterxml.jackson.annotation`.

### Adding a type

**New exercise activity:**
- A domain VO in `domain/exercise/valueobject/activity`.
- An `IStepGeneration`, registered as a `@Bean` in `DomainServiceConfig`.
- A DTO, an `ExerciseActivityType` constant, and an `IExerciseActivityDTOProvider`.
- An `IExerciseActivityDTOToDomainMapper` with an `ExerciseActivityMapperKey` constant.
- An entity, a `@JsonSubTypes` entry on `ExerciseActivityEntity`, and an `IExerciseActivityEntityToDomainMapper` with an `ExerciseActivityEntityMapperKey` constant.

**New step context:** the same shape.
- A `StepContext` VO and a `StepType` constant.
- A `PracticeStepDTO` subclass, a `PracticeStepType` constant, and an `IPracticeStepDTOProvider`.
- An `IStepContextMapper` with a `StepContextMapperKey` constant.
- An entity, a `@JsonSubTypes` entry on `StepContextEntity`, and an `IStepContextEntityToDomainMapper` with a `StepContextEntityMapperKey` constant.

**Tests to update:**
- The four `*MapperFactoryTest`s build their registries by hand, so add the new mapper and a case to each.
- `PolymorphicDtoSerializationTest` `@Import`s every DTO provider explicitly, so add the new provider there.

Discriminator strings are persisted in the DB and appear in `import.sql`. `PersistedJsonCompatibilityTest` pins that format, so treat the strings as a stable contract.

# Sound Generation Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the MIDI-bound, WAV-only interval audio pipeline in `vn.ktt.music` with a technology-agnostic pipeline (Score → synthesizer port → encoder port). Sound settings become configurable: they are saved as defaults and can be overridden per request.

**Architecture:**
- **Domain:** a pure `IntervalScoreComposer` turns musical intent into a MIDI-free `Score`.
- **Application:** use cases resolve `SoundSettings` (request override, then saved default, then built-in default) and call a `SoundRenderingService`. That service runs an `ISoundSynthesizerPort`, then an `IAudioEncoderPort` picked from `AudioEncoderRegistry` by `AudioFormat`.
- **Infrastructure:** MIDI lives only in `MidiSoundSynthesizer`, and WAV only in `WavAudioEncoder`.

**Tech Stack:** Java 25, Spring Boot 4.1.1, Spring Data JPA (PostgreSQL), Jackson 3 (`tools.jackson.*`), `javax.sound.midi`/`javax.sound.sampled`, JUnit 5, Mockito (from `spring-boot-starter-test`), and `spring-boot-starter-webmvc-test` for `@WebMvcTest`.

**Spec:** `docs/superpowers/specs/2026-09-23-sound-generation-use-cases-design.md`. Read it before starting any task. Section numbers below (§N) refer to it.

## Global Constraints

**Build**
- JDK 25 and a system `mvn`; there is no Maven wrapper.
- `mvn test` must pass after every task, with no database, no SF2 file and no `--add-exports`.
- `mvn spring-boot:run` is only expected to start again from the end of Task 5. Tasks 3–4 add a `@Service` whose synthesizer port has no bean until Task 5.

**Architecture rules**
- Domain classes (`vn.ktt.music.domain..`) have no Spring annotations, and nothing in domain or application imports `javax.sound.*`.
- Domain services are wired as `@Bean`s in `infrastructure/config/MusicalDomainServiceConfig`.
- Invalid input is a plain `IllegalArgumentException`, from domain and application.
- Infrastructure render and encode failures throw `vn.ktt.music.infrastructure.audio.AudioGenerationException`.
- Don't add `@ControllerAdvice`, `@Transactional`, or per-controller `try`/`catch`.

**Values fixed by the spec**
- Bounds: `noteDurationMs` 50–4000, `silenceMs` 0–2000, `loudness` 1–100.
- Defaults: `SoundSettings(188, 21, 71, PIANO)`.
- `PREROLL_MS = 63`.
- Stacked notes are held for `2 × noteDurationMs`.
- `velocity = round(loudness × 127 / 100)`.
- `ScoreToMidiSequenceConverter.PPQ = 500`, so 1 tick = 1 ms at the default tempo. The converter writes a single track with no tempo meta event.

**Parsing**
- `fromString` parsers (`InstrumentType`, `Direction`, `AudioFormat`; `Interval.Texture` already does this) ignore case and throw `IllegalArgumentException` for an unknown or `null` value.
- A `null` `overrides` is an empty patch, and a `null` `format` is `WAV`.

**Output**
- File names are `interval-<notation>.<ext>` and `interval-range-<notation>.<ext>`.
- MIME type and extension come from `EncodedAudio`.

**Seed data**
- `import.sql` statements stay on one line each.

**Git**
- Work on branch `refactor/5-sound-generation`.
- Every commit message ends with this trailer:
  `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`

## Review Focus

These are the inputs most likely to bite a real client. Each one is pinned by the test named in its owning task.

1. **Case variations in query parameters** (`texture=descending`, `direction=down`, `instrument=piano`, `format=Wav`) are accepted exactly as the upper-case forms are. Pinned in Task 1 (`fromString` tests) and Task 7 (`acceptsAnyCaseForEveryParameter`).
2. **Legacy requests with no overrides and no `format`** keep working and return WAV built from the saved settings. Pinned in Task 7 (`nullOverridesAndFormatFallBackToSavedSettingsAndWav`) and Task 8 (`legacyRequestSendsEmptyOverridesAndNoFormat`).
3. **A unison (`P0`) interval, especially `STACKED`**, renders without error, even though its two notes share a MIDI number. Pinned in Task 2 (`singleUnisonStackedGivesTwoIdenticalNotes`) and Task 5 (`unisonStackedNotesRender`).
4. **A PATCH with explicit `null`s or only some fields** changes only the fields that are present and non-null. Pinned in Task 7 (`explicitNullsChangeNothing`, `partialPatchKeepsOtherFields`) and Task 8 (`patchBindsPartialBodyWithExplicitNull`).
5. **An empty `musical_config` table** yields `SoundSettings.defaults()` instead of a 500. Pinned in Task 6 (`emptyTableGivesDefaults`).

---

### Task 1: Domain value objects and parsing

**Files:**
- Create: `src/main/java/vn/ktt/music/domain/sound/valueobject/SoundSettings.java`
- Create: `src/main/java/vn/ktt/music/domain/sound/valueobject/NoteEvent.java`
- Create: `src/main/java/vn/ktt/music/domain/sound/valueobject/Score.java`
- Create: `src/main/java/vn/ktt/music/domain/sound/valueobject/Direction.java`
- Modify: `src/main/java/vn/ktt/music/domain/instrument/InstrumentType.java`
- Test: `src/test/java/vn/ktt/music/domain/sound/valueobject/SoundSettingsTest.java`
- Test: `src/test/java/vn/ktt/music/domain/sound/valueobject/NoteEventTest.java`
- Test: `src/test/java/vn/ktt/music/domain/sound/valueobject/ScoreTest.java`
- Test: `src/test/java/vn/ktt/music/domain/sound/valueobject/DirectionTest.java`
- Test: `src/test/java/vn/ktt/music/domain/instrument/InstrumentTypeTest.java`

**Interfaces:**
- Consumes: `vn.ktt.music.domain.atom.Pitch` (record, `toMidiNumber()`, `static convertFromMidiNumber(int)`), and `vn.ktt.music.domain.instrument.InstrumentType` (enum `PIANO`).
- Produces:
  - `record SoundSettings(int noteDurationMs, int silenceMs, int loudness, InstrumentType instrument)`, with:
    - `static SoundSettings defaults()`
    - `SoundSettings withOverrides(Integer, Integer, Integer, InstrumentType)`
    - `public static final int MIN_NOTE_DURATION_MS, MAX_NOTE_DURATION_MS, MIN_SILENCE_MS, MAX_SILENCE_MS, MIN_LOUDNESS, MAX_LOUDNESS`
  - `record NoteEvent(Pitch pitch, long onsetMs, long durationMs, int loudness)`
  - `record Score(List<NoteEvent> events)`
  - `enum Direction { UP, DOWN; static Direction fromString(String) }`
  - `static InstrumentType InstrumentType.fromString(String)`

- [ ] **Step 1: Write the failing tests**

`src/test/java/vn/ktt/music/domain/sound/valueobject/SoundSettingsTest.java`:

```java
package vn.ktt.music.domain.sound.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import vn.ktt.music.domain.instrument.InstrumentType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SoundSettingsTest {

    @Test
    void defaultsMatchTodaysSound() {
        assertEquals(new SoundSettings(188, 21, 71, InstrumentType.PIANO), SoundSettings.defaults());
    }

    @ParameterizedTest
    @CsvSource({"50, 0, 1", "4000, 2000, 100"})
    void acceptsValuesOnTheBounds(int noteDurationMs, int silenceMs, int loudness) {
        var settings = new SoundSettings(noteDurationMs, silenceMs, loudness, InstrumentType.PIANO);

        assertEquals(noteDurationMs, settings.noteDurationMs());
        assertEquals(silenceMs, settings.silenceMs());
        assertEquals(loudness, settings.loudness());
    }

    @ParameterizedTest
    @CsvSource({"49, 21, 71", "4001, 21, 71", "188, -1, 71", "188, 2001, 71", "188, 21, 0", "188, 21, 101"})
    void rejectsValuesOutsideTheBounds(int noteDurationMs, int silenceMs, int loudness) {
        assertThrows(IllegalArgumentException.class,
                () -> new SoundSettings(noteDurationMs, silenceMs, loudness, InstrumentType.PIANO));
    }

    @Test
    void rejectsMissingInstrument() {
        assertThrows(IllegalArgumentException.class, () -> new SoundSettings(188, 21, 71, null));
    }

    @Test
    void withOverridesReplacesOnlyNonNullFields() {
        var result = SoundSettings.defaults().withOverrides(300, null, 50, null);

        assertEquals(new SoundSettings(300, 21, 50, InstrumentType.PIANO), result);
    }

    @Test
    void withOverridesAllNullKeepsEverything() {
        assertEquals(SoundSettings.defaults(), SoundSettings.defaults().withOverrides(null, null, null, null));
    }

    @Test
    void withOverridesValidatesTheResult() {
        assertThrows(IllegalArgumentException.class,
                () -> SoundSettings.defaults().withOverrides(null, null, 0, null));
    }
}
```

`src/test/java/vn/ktt/music/domain/sound/valueobject/NoteEventTest.java`:

```java
package vn.ktt.music.domain.sound.valueobject;

import org.junit.jupiter.api.Test;
import vn.ktt.music.domain.atom.Pitch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NoteEventTest {

    private static final Pitch C4 = Pitch.convertFromMidiNumber(60);

    @Test
    void acceptsAValidNote() {
        var note = new NoteEvent(C4, 0, 1, 1);

        assertEquals(60, note.pitch().toMidiNumber());
    }

    @Test
    void rejectsMissingPitch() {
        assertThrows(IllegalArgumentException.class, () -> new NoteEvent(null, 0, 100, 50));
    }

    @Test
    void rejectsNegativeOnset() {
        assertThrows(IllegalArgumentException.class, () -> new NoteEvent(C4, -1, 100, 50));
    }

    @Test
    void rejectsNonPositiveDuration() {
        assertThrows(IllegalArgumentException.class, () -> new NoteEvent(C4, 0, 0, 50));
    }

    @Test
    void rejectsLoudnessOutsideBounds() {
        assertThrows(IllegalArgumentException.class, () -> new NoteEvent(C4, 0, 100, 0));
        assertThrows(IllegalArgumentException.class, () -> new NoteEvent(C4, 0, 100, 101));
    }
}
```

`src/test/java/vn/ktt/music/domain/sound/valueobject/ScoreTest.java`:

```java
package vn.ktt.music.domain.sound.valueobject;

import org.junit.jupiter.api.Test;
import vn.ktt.music.domain.atom.Pitch;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScoreTest {

    private static final NoteEvent NOTE = new NoteEvent(Pitch.convertFromMidiNumber(60), 0, 100, 50);

    @Test
    void rejectsEmptyOrMissingEvents() {
        assertThrows(IllegalArgumentException.class, () -> new Score(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new Score(null));
    }

    @Test
    void keepsADefensiveUnmodifiableCopy() {
        var source = new ArrayList<>(List.of(NOTE));
        var score = new Score(source);

        source.add(NOTE);

        assertEquals(1, score.events().size());
        assertThrows(UnsupportedOperationException.class, () -> score.events().add(NOTE));
    }
}
```

`src/test/java/vn/ktt/music/domain/sound/valueobject/DirectionTest.java`:

```java
package vn.ktt.music.domain.sound.valueobject;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DirectionTest {

    @ParameterizedTest
    @ValueSource(strings = {"UP", "up", "Up"})
    void parsesUpInAnyCase(String value) {
        assertEquals(Direction.UP, Direction.fromString(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"DOWN", "down"})
    void parsesDownInAnyCase(String value) {
        assertEquals(Direction.DOWN, Direction.fromString(value));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "sideways"})
    void rejectsUnknownOrMissingValues(String value) {
        assertThrows(IllegalArgumentException.class, () -> Direction.fromString(value));
    }
}
```

`src/test/java/vn/ktt/music/domain/instrument/InstrumentTypeTest.java`:

```java
package vn.ktt.music.domain.instrument;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InstrumentTypeTest {

    @ParameterizedTest
    @ValueSource(strings = {"PIANO", "piano", "Piano"})
    void parsesInAnyCase(String value) {
        assertEquals(InstrumentType.PIANO, InstrumentType.fromString(value));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "violin"})
    void rejectsUnknownOrMissingValues(String value) {
        assertThrows(IllegalArgumentException.class, () -> InstrumentType.fromString(value));
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn test -Dtest='SoundSettingsTest,NoteEventTest,ScoreTest,DirectionTest,InstrumentTypeTest'`
Expected: BUILD FAILURE with `cannot find symbol` for `SoundSettings`, `NoteEvent`, `Score`, `Direction` and `InstrumentType.fromString`.

- [ ] **Step 3: Implement**

`src/main/java/vn/ktt/music/domain/sound/valueobject/SoundSettings.java`:

```java
package vn.ktt.music.domain.sound.valueobject;

import vn.ktt.music.domain.instrument.InstrumentType;

public record SoundSettings(int noteDurationMs, int silenceMs, int loudness, InstrumentType instrument) {

    public static final int MIN_NOTE_DURATION_MS = 50;
    public static final int MAX_NOTE_DURATION_MS = 4000;
    public static final int MIN_SILENCE_MS = 0;
    public static final int MAX_SILENCE_MS = 2000;
    public static final int MIN_LOUDNESS = 1;
    public static final int MAX_LOUDNESS = 100;

    public SoundSettings {
        requireInRange("noteDurationMs", noteDurationMs, MIN_NOTE_DURATION_MS, MAX_NOTE_DURATION_MS);
        requireInRange("silenceMs", silenceMs, MIN_SILENCE_MS, MAX_SILENCE_MS);
        requireInRange("loudness", loudness, MIN_LOUDNESS, MAX_LOUDNESS);
        if (instrument == null) {
            throw new IllegalArgumentException("Instrument is required");
        }
    }

    // Converted from the old MIDI constants (PPQ 480 at 120 BPM): 180 ticks, 20 ticks, velocity 90.
    public static SoundSettings defaults() {
        return new SoundSettings(188, 21, 71, InstrumentType.PIANO);
    }

    public SoundSettings withOverrides(Integer noteDurationMs, Integer silenceMs, Integer loudness,
                                       InstrumentType instrument) {
        return new SoundSettings(
                noteDurationMs != null ? noteDurationMs : this.noteDurationMs,
                silenceMs != null ? silenceMs : this.silenceMs,
                loudness != null ? loudness : this.loudness,
                instrument != null ? instrument : this.instrument);
    }

    private static void requireInRange(String field, int value, int min, int max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(field + " must be between " + min + " and " + max + ": " + value);
        }
    }
}
```

`src/main/java/vn/ktt/music/domain/sound/valueobject/NoteEvent.java`:

```java
package vn.ktt.music.domain.sound.valueobject;

import vn.ktt.music.domain.atom.Pitch;

public record NoteEvent(Pitch pitch, long onsetMs, long durationMs, int loudness) {

    public NoteEvent {
        if (pitch == null) {
            throw new IllegalArgumentException("Pitch is required");
        }
        if (onsetMs < 0) {
            throw new IllegalArgumentException("Onset must not be negative: " + onsetMs);
        }
        if (durationMs <= 0) {
            throw new IllegalArgumentException("Duration must be positive: " + durationMs);
        }
        if (loudness < SoundSettings.MIN_LOUDNESS || loudness > SoundSettings.MAX_LOUDNESS) {
            throw new IllegalArgumentException("Loudness out of range: " + loudness);
        }
    }
}
```

`src/main/java/vn/ktt/music/domain/sound/valueobject/Score.java`:

```java
package vn.ktt.music.domain.sound.valueobject;

import java.util.List;

public record Score(List<NoteEvent> events) {

    public Score {
        if (events == null || events.isEmpty()) {
            throw new IllegalArgumentException("A score needs at least one note");
        }
        events = List.copyOf(events);
    }
}
```

`src/main/java/vn/ktt/music/domain/sound/valueobject/Direction.java`:

```java
package vn.ktt.music.domain.sound.valueobject;

public enum Direction {
    UP,
    DOWN;

    public static Direction fromString(String direction) {
        for (Direction value : values()) {
            if (value.name().equalsIgnoreCase(direction)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown direction: " + direction);
    }
}
```

Replace the body of `src/main/java/vn/ktt/music/domain/instrument/InstrumentType.java` with:

```java
package vn.ktt.music.domain.instrument;

public enum InstrumentType {
    PIANO;

    public static InstrumentType fromString(String instrument) {
        for (InstrumentType value : values()) {
            if (value.name().equalsIgnoreCase(instrument)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown instrument: " + instrument);
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `mvn test -Dtest='SoundSettingsTest,NoteEventTest,ScoreTest,DirectionTest,InstrumentTypeTest'`
Expected: BUILD SUCCESS, with every test passing.

- [ ] **Step 5: Run the whole suite**

Run: `mvn test`
Expected: BUILD SUCCESS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/vn/ktt/music/domain/sound src/main/java/vn/ktt/music/domain/instrument/InstrumentType.java \
        src/test/java/vn/ktt/music/domain
git commit -F - <<'EOF'
feat(music): add sound settings, score and direction value objects

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
EOF
```

---

### Task 2: IntervalScoreComposer domain service

**Files:**
- Create: `src/main/java/vn/ktt/music/domain/service/IIntervalScoreComposer.java`
- Create: `src/main/java/vn/ktt/music/domain/service/IntervalScoreComposer.java`
- Modify: `src/main/java/vn/ktt/music/infrastructure/config/MusicalDomainServiceConfig.java`
- Test: `src/test/java/vn/ktt/music/domain/service/IntervalScoreComposerTest.java`

**Interfaces:**
- Consumes:
  - Task 1: `SoundSettings`, `NoteEvent`, `Score`, `Direction`.
  - Existing:
    - `Interval` (`getIntervalType().getHalfSteps()`, the nested enum `Interval.Texture { ASCENDING, DESCENDING, STACKED }`, `new Interval(Interval.IntervalType)`)
    - `Instrument` (`getLowestPitch()`, `getHighestPitch()`, `static reconstruct(IMusicalEntityFactory, InstrumentType, String, String)`)
    - `MusicalEntityFactory`
- Produces:
  - `interface IIntervalScoreComposer` with:
    - `Score single(Interval interval, Interval.Texture texture, Pitch start, SoundSettings settings)`
    - `Score range(Interval interval, Interval.Texture texture, Direction direction, Instrument instrument, SoundSettings settings)`
  - `IntervalScoreComposer.PREROLL_MS = 63`
  - A `@Bean IIntervalScoreComposer intervalScoreComposer()` in `MusicalDomainServiceConfig`

- [ ] **Step 1: Write the failing test**

`src/test/java/vn/ktt/music/domain/service/IntervalScoreComposerTest.java`:

```java
package vn.ktt.music.domain.service;

import org.junit.jupiter.api.Test;
import vn.ktt.music.domain.atom.Pitch;
import vn.ktt.music.domain.composition.Interval;
import vn.ktt.music.domain.factory.MusicalEntityFactory;
import vn.ktt.music.domain.instrument.Instrument;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.domain.sound.valueobject.Direction;
import vn.ktt.music.domain.sound.valueobject.NoteEvent;
import vn.ktt.music.domain.sound.valueobject.Score;
import vn.ktt.music.domain.sound.valueobject.SoundSettings;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IntervalScoreComposerTest {

    private static final SoundSettings DEFAULTS = SoundSettings.defaults();

    private final IntervalScoreComposer composer = new IntervalScoreComposer();
    private final MusicalEntityFactory factory = new MusicalEntityFactory();
    private final Instrument piano = Instrument.reconstruct(factory, InstrumentType.PIANO, "A0", "C8");

    private static Interval interval(Interval.IntervalType type) {
        return new Interval(type);
    }

    private static Pitch midi(int number) {
        return Pitch.convertFromMidiNumber(number);
    }

    private static int midiOf(NoteEvent note) {
        return note.pitch().toMidiNumber();
    }

    @Test
    void singleAscendingPlaysLowerThenUpper() {
        Score score = composer.single(interval(Interval.IntervalType.MAJOR_3RD), Interval.Texture.ASCENDING,
                midi(60), DEFAULTS);

        assertEquals(List.of(
                new NoteEvent(midi(60), 63, 188, 71),
                new NoteEvent(midi(64), 272, 188, 71)), score.events());
    }

    @Test
    void singleDescendingPlaysUpperFirst() {
        Score score = composer.single(interval(Interval.IntervalType.MAJOR_3RD), Interval.Texture.DESCENDING,
                midi(60), DEFAULTS);

        assertEquals(List.of(
                new NoteEvent(midi(64), 63, 188, 71),
                new NoteEvent(midi(60), 272, 188, 71)), score.events());
    }

    @Test
    void singleStackedStartsTogetherAndHoldsTwiceAsLong() {
        Score score = composer.single(interval(Interval.IntervalType.MAJOR_3RD), Interval.Texture.STACKED,
                midi(60), DEFAULTS);

        assertEquals(List.of(
                new NoteEvent(midi(60), 63, 376, 71),
                new NoteEvent(midi(64), 63, 376, 71)), score.events());
    }

    @Test
    void singleUnisonStackedGivesTwoIdenticalNotes() {
        Score score = composer.single(interval(Interval.IntervalType.UNISON), Interval.Texture.STACKED,
                midi(60), DEFAULTS);

        assertEquals(List.of(
                new NoteEvent(midi(60), 63, 376, 71),
                new NoteEvent(midi(60), 63, 376, 71)), score.events());
    }

    @Test
    void singleUsesTheGivenSettings() {
        var settings = new SoundSettings(300, 50, 40, InstrumentType.PIANO);

        Score score = composer.single(interval(Interval.IntervalType.MAJOR_3RD), Interval.Texture.ASCENDING,
                midi(60), settings);

        assertEquals(List.of(
                new NoteEvent(midi(60), 63, 300, 40),
                new NoteEvent(midi(64), 413, 300, 40)), score.events());
    }

    @Test
    void rangeUpAscendingMatchesTodaysScheduleInMilliseconds() {
        Score score = composer.range(interval(Interval.IntervalType.PERFECT_5TH), Interval.Texture.ASCENDING,
                Direction.UP, piano, DEFAULTS);

        List<NoteEvent> events = score.events();
        assertEquals(16, events.size());
        for (int k = 0; k < events.size(); k++) {
            int base = 21 + k / 2;
            int expectedMidi = k % 2 == 0 ? base : base + 7;
            assertEquals(expectedMidi, midiOf(events.get(k)), "pitch of event " + k);
            assertEquals(63 + 209L * k, events.get(k).onsetMs(), "onset of event " + k);
            assertEquals(188, events.get(k).durationMs(), "duration of event " + k);
        }
    }

    @Test
    void rangeDownSweepsFromTheTopBase() {
        Score score = composer.range(interval(Interval.IntervalType.PERFECT_5TH), Interval.Texture.ASCENDING,
                Direction.DOWN, piano, DEFAULTS);

        List<NoteEvent> events = score.events();
        assertEquals(28, midiOf(events.get(0)));
        assertEquals(35, midiOf(events.get(1)));
        assertEquals(21, midiOf(events.get(14)));
        assertEquals(28, midiOf(events.get(15)));
    }

    @Test
    void rangeStackedMatchesTodaysScheduleInMilliseconds() {
        Score score = composer.range(interval(Interval.IntervalType.PERFECT_5TH), Interval.Texture.STACKED,
                Direction.UP, piano, DEFAULTS);

        List<NoteEvent> events = score.events();
        assertEquals(16, events.size());
        for (int pair = 0; pair < 8; pair++) {
            NoteEvent lower = events.get(2 * pair);
            NoteEvent upper = events.get(2 * pair + 1);
            assertEquals(21 + pair, midiOf(lower));
            assertEquals(28 + pair, midiOf(upper));
            assertEquals(63 + 397L * pair, lower.onsetMs());
            assertEquals(lower.onsetMs(), upper.onsetMs());
            assertEquals(376, lower.durationMs());
            assertEquals(376, upper.durationMs());
        }
    }

    @Test
    void rangeDescendingTexturePlaysUpperFirstInEachPair() {
        Score score = composer.range(interval(Interval.IntervalType.MAJOR_3RD), Interval.Texture.DESCENDING,
                Direction.UP, piano, DEFAULTS);

        assertEquals(25, midiOf(score.events().get(0)));
        assertEquals(21, midiOf(score.events().get(1)));
    }

    @Test
    void rangeFitsExactlyWhenTopNoteIsTheInstrumentsHighest() {
        var narrow = Instrument.reconstruct(factory, InstrumentType.PIANO, "C4", "G#4");

        Score score = composer.range(interval(Interval.IntervalType.MAJOR_3RD), Interval.Texture.ASCENDING,
                Direction.UP, narrow, DEFAULTS);

        assertEquals(10, score.events().size());
        assertEquals(68, midiOf(score.events().get(9)));
    }

    @Test
    void rangeThrowsWhenInstrumentIsTooNarrow() {
        var narrow = Instrument.reconstruct(factory, InstrumentType.PIANO, "C4", "E4");

        assertThrows(IllegalArgumentException.class, () -> composer.range(
                interval(Interval.IntervalType.MAJOR_3RD), Interval.Texture.ASCENDING, Direction.UP, narrow, DEFAULTS));
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `mvn test -Dtest=IntervalScoreComposerTest`
Expected: BUILD FAILURE with `cannot find symbol: class IntervalScoreComposer`.

- [ ] **Step 3: Implement**

`src/main/java/vn/ktt/music/domain/service/IIntervalScoreComposer.java`:

```java
package vn.ktt.music.domain.service;

import vn.ktt.music.domain.atom.Pitch;
import vn.ktt.music.domain.composition.Interval;
import vn.ktt.music.domain.instrument.Instrument;
import vn.ktt.music.domain.sound.valueobject.Direction;
import vn.ktt.music.domain.sound.valueobject.Score;
import vn.ktt.music.domain.sound.valueobject.SoundSettings;

public interface IIntervalScoreComposer {
    Score single(Interval interval, Interval.Texture texture, Pitch start, SoundSettings settings);

    Score range(Interval interval, Interval.Texture texture, Direction direction, Instrument instrument,
                SoundSettings settings);
}
```

`src/main/java/vn/ktt/music/domain/service/IntervalScoreComposer.java`:

```java
package vn.ktt.music.domain.service;

import vn.ktt.music.domain.atom.Pitch;
import vn.ktt.music.domain.composition.Interval;
import vn.ktt.music.domain.instrument.Instrument;
import vn.ktt.music.domain.sound.valueobject.Direction;
import vn.ktt.music.domain.sound.valueobject.NoteEvent;
import vn.ktt.music.domain.sound.valueobject.Score;
import vn.ktt.music.domain.sound.valueobject.SoundSettings;

import java.util.ArrayList;
import java.util.List;

public class IntervalScoreComposer implements IIntervalScoreComposer {

    public static final long PREROLL_MS = 63;

    @Override
    public Score single(Interval interval, Interval.Texture texture, Pitch start, SoundSettings settings) {
        List<NoteEvent> events = new ArrayList<>();
        appendInterval(events, PREROLL_MS, start.toMidiNumber(), interval.getIntervalType().getHalfSteps(),
                texture, settings);
        return new Score(events);
    }

    // Sweeps the lower note over [lowest, lowest + halfSteps], as the MIDI builder did before.
    @Override
    public Score range(Interval interval, Interval.Texture texture, Direction direction, Instrument instrument,
                       SoundSettings settings) {
        int halfSteps = interval.getIntervalType().getHalfSteps();
        int lowest = instrument.getLowestPitch().toMidiNumber();
        int highest = instrument.getHighestPitch().toMidiNumber();
        if (lowest + 2 * halfSteps > highest) {
            throw new IllegalArgumentException("Interval " + interval + " range does not fit the instrument range");
        }

        List<NoteEvent> events = new ArrayList<>();
        long onset = PREROLL_MS;
        for (int step = 0; step <= halfSteps; step++) {
            int base = direction == Direction.UP ? lowest + step : lowest + halfSteps - step;
            onset = appendInterval(events, onset, base, halfSteps, texture, settings);
        }
        return new Score(events);
    }

    private long appendInterval(List<NoteEvent> events, long onset, int lowerMidi, int halfSteps,
                                Interval.Texture texture, SoundSettings settings) {
        Pitch lower = Pitch.convertFromMidiNumber(lowerMidi);
        Pitch upper = Pitch.convertFromMidiNumber(lowerMidi + halfSteps);
        long noteMs = settings.noteDurationMs();
        long stepMs = noteMs + settings.silenceMs();
        int loudness = settings.loudness();

        return switch (texture) {
            case STACKED -> {
                long stackedMs = 2 * noteMs;
                events.add(new NoteEvent(lower, onset, stackedMs, loudness));
                events.add(new NoteEvent(upper, onset, stackedMs, loudness));
                yield onset + stackedMs + settings.silenceMs();
            }
            case ASCENDING -> {
                events.add(new NoteEvent(lower, onset, noteMs, loudness));
                events.add(new NoteEvent(upper, onset + stepMs, noteMs, loudness));
                yield onset + 2 * stepMs;
            }
            case DESCENDING -> {
                events.add(new NoteEvent(upper, onset, noteMs, loudness));
                events.add(new NoteEvent(lower, onset + stepMs, noteMs, loudness));
                yield onset + 2 * stepMs;
            }
        };
    }
}
```

Replace `src/main/java/vn/ktt/music/infrastructure/config/MusicalDomainServiceConfig.java` with:

```java
package vn.ktt.music.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.ktt.music.domain.service.IIntervalScoreComposer;
import vn.ktt.music.domain.service.IMusicalOperation;
import vn.ktt.music.domain.service.IntervalScoreComposer;
import vn.ktt.music.domain.service.MusicalOperation;

@Configuration
public class MusicalDomainServiceConfig {

    @Bean
    public IMusicalOperation musicalOperation() {
        return new MusicalOperation();
    }

    @Bean
    public IIntervalScoreComposer intervalScoreComposer() {
        return new IntervalScoreComposer();
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `mvn test -Dtest=IntervalScoreComposerTest`
Expected: BUILD SUCCESS, with 11 tests passing.

Run: `mvn test`
Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/vn/ktt/music/domain/service src/main/java/vn/ktt/music/infrastructure/config/MusicalDomainServiceConfig.java \
        src/test/java/vn/ktt/music/domain/service
git commit -F - <<'EOF'
feat(music): compose interval scores in the domain

Moves the note scheduling out of MidiSequenceBuilder into a MIDI-free
domain service, keeping today's timings in milliseconds.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
EOF
```

---

### Task 3: Application audio pipeline (payloads, ports, encoder registry, rendering service)

**Files:**
- Create: `src/main/java/vn/ktt/music/application/sound/audio/AudioFormat.java`
- Create: `src/main/java/vn/ktt/music/application/sound/audio/PcmAudio.java`
- Create: `src/main/java/vn/ktt/music/application/sound/audio/EncodedAudio.java`
- Create: `src/main/java/vn/ktt/music/application/sound/outbound/ISoundSynthesizerPort.java`
- Create: `src/main/java/vn/ktt/music/application/sound/outbound/IAudioEncoderPort.java`
- Create: `src/main/java/vn/ktt/music/application/sound/AudioEncoderRegistry.java`
- Create: `src/main/java/vn/ktt/music/application/sound/SoundRenderingService.java`
- Modify (replace): `src/main/java/vn/ktt/music/application/sound/dto/AudioContent.java`
- Modify: `src/main/java/vn/ktt/music/application/sound/IntervalGeneratorService.java`, the `toAudioContent` method only. This is a temporary edit; Task 9 deletes the file.
- Modify: `src/main/java/vn/ktt/music/infrastructure/controller/IntervalsController.java`, `IntervalRangeController.java`, switching to the record accessors only.
- Test support: `src/test/java/vn/ktt/music/application/support/FakeSoundSynthesizer.java`, `FakeAudioEncoder.java`
- Test: `src/test/java/vn/ktt/music/application/sound/audio/AudioFormatTest.java`
- Test: `src/test/java/vn/ktt/music/application/sound/AudioEncoderRegistryTest.java`
- Test: `src/test/java/vn/ktt/music/application/sound/SoundRenderingServiceTest.java`

**Interfaces:**
- Consumes: Task 1's `Score` and `InstrumentType`.
- Produces:
  - `enum AudioFormat { WAV; static AudioFormat fromString(String) }`
  - `record PcmAudio(float[] samples, float sampleRate, int channels)`
  - `record EncodedAudio(byte[] data, String mimeType, String extension)`
  - `interface ISoundSynthesizerPort { PcmAudio synthesize(Score score, InstrumentType instrument); }`
  - `interface IAudioEncoderPort { AudioFormat format(); EncodedAudio encode(PcmAudio audio); }`
  - `@Component AudioEncoderRegistry(List<IAudioEncoderPort>)`, with `IAudioEncoderPort get(AudioFormat)` and `List<AudioFormat> supportedFormats()`
  - `@Service SoundRenderingService(ISoundSynthesizerPort, AudioEncoderRegistry)`, with `AudioContent render(Score, InstrumentType, AudioFormat, String baseName)`
  - `record AudioContent(byte[] data, String mimeType, String fileName)`
  - Test fakes:
    - `vn.ktt.music.application.support.FakeSoundSynthesizer` (public fields `int calls`, `Score lastScore`, `InstrumentType lastInstrument`)
    - `FakeAudioEncoder(AudioFormat)`, whose MIME type is `"audio/" + lowercase name` and whose extension is the lowercase name

- [ ] **Step 1: Write the test fakes and failing tests**

`src/test/java/vn/ktt/music/application/support/FakeSoundSynthesizer.java`:

```java
package vn.ktt.music.application.support;

import vn.ktt.music.application.sound.audio.PcmAudio;
import vn.ktt.music.application.sound.outbound.ISoundSynthesizerPort;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.domain.sound.valueobject.Score;

public class FakeSoundSynthesizer implements ISoundSynthesizerPort {
    public int calls;
    public Score lastScore;
    public InstrumentType lastInstrument;

    @Override
    public PcmAudio synthesize(Score score, InstrumentType instrument) {
        calls++;
        lastScore = score;
        lastInstrument = instrument;
        return new PcmAudio(new float[]{0f}, 44100f, 1);
    }
}
```

`src/test/java/vn/ktt/music/application/support/FakeAudioEncoder.java`:

```java
package vn.ktt.music.application.support;

import vn.ktt.music.application.sound.audio.AudioFormat;
import vn.ktt.music.application.sound.audio.EncodedAudio;
import vn.ktt.music.application.sound.audio.PcmAudio;
import vn.ktt.music.application.sound.outbound.IAudioEncoderPort;

public class FakeAudioEncoder implements IAudioEncoderPort {
    private final AudioFormat format;

    public FakeAudioEncoder(AudioFormat format) {
        this.format = format;
    }

    @Override
    public AudioFormat format() {
        return format;
    }

    @Override
    public EncodedAudio encode(PcmAudio audio) {
        String extension = format.name().toLowerCase();
        return new EncodedAudio(new byte[]{1, 2, 3}, "audio/" + extension, extension);
    }
}
```

`src/test/java/vn/ktt/music/application/sound/audio/AudioFormatTest.java`:

```java
package vn.ktt.music.application.sound.audio;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AudioFormatTest {

    @ParameterizedTest
    @ValueSource(strings = {"WAV", "wav", "Wav"})
    void parsesInAnyCase(String value) {
        assertEquals(AudioFormat.WAV, AudioFormat.fromString(value));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "mp3"})
    void rejectsUnknownOrMissingValues(String value) {
        assertThrows(IllegalArgumentException.class, () -> AudioFormat.fromString(value));
    }
}
```

`src/test/java/vn/ktt/music/application/sound/AudioEncoderRegistryTest.java`:

```java
package vn.ktt.music.application.sound;

import org.junit.jupiter.api.Test;
import vn.ktt.music.application.sound.audio.AudioFormat;
import vn.ktt.music.application.support.FakeAudioEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AudioEncoderRegistryTest {

    @Test
    void returnsTheEncoderForAFormat() {
        var wav = new FakeAudioEncoder(AudioFormat.WAV);
        var registry = new AudioEncoderRegistry(List.of(wav));

        assertSame(wav, registry.get(AudioFormat.WAV));
    }

    @Test
    void rejectsTwoEncodersForTheSameFormat() {
        var encoders = List.of(new FakeAudioEncoder(AudioFormat.WAV), new FakeAudioEncoder(AudioFormat.WAV));

        assertThrows(IllegalStateException.class, () -> new AudioEncoderRegistry(encoders));
    }

    @Test
    void rejectsAFormatWithoutEncoder() {
        var registry = new AudioEncoderRegistry(List.of());

        assertThrows(IllegalArgumentException.class, () -> registry.get(AudioFormat.WAV));
    }

    @Test
    void listsSupportedFormats() {
        var registry = new AudioEncoderRegistry(List.of(new FakeAudioEncoder(AudioFormat.WAV)));

        assertEquals(List.of(AudioFormat.WAV), registry.supportedFormats());
    }
}
```

`src/test/java/vn/ktt/music/application/sound/SoundRenderingServiceTest.java`:

```java
package vn.ktt.music.application.sound;

import org.junit.jupiter.api.Test;
import vn.ktt.music.application.sound.audio.AudioFormat;
import vn.ktt.music.application.sound.dto.AudioContent;
import vn.ktt.music.application.support.FakeAudioEncoder;
import vn.ktt.music.application.support.FakeSoundSynthesizer;
import vn.ktt.music.domain.atom.Pitch;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.domain.sound.valueobject.NoteEvent;
import vn.ktt.music.domain.sound.valueobject.Score;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SoundRenderingServiceTest {

    private static final Score SCORE = new Score(List.of(new NoteEvent(Pitch.convertFromMidiNumber(60), 0, 100, 50)));

    private final FakeSoundSynthesizer synthesizer = new FakeSoundSynthesizer();

    @Test
    void synthesizesEncodesAndNamesTheFile() {
        var service = new SoundRenderingService(synthesizer,
                new AudioEncoderRegistry(List.of(new FakeAudioEncoder(AudioFormat.WAV))));

        AudioContent content = service.render(SCORE, InstrumentType.PIANO, AudioFormat.WAV, "interval-M3");

        assertSame(SCORE, synthesizer.lastScore);
        assertEquals(InstrumentType.PIANO, synthesizer.lastInstrument);
        assertArrayEquals(new byte[]{1, 2, 3}, content.data());
        assertEquals("audio/wav", content.mimeType());
        assertEquals("interval-M3.wav", content.fileName());
    }

    @Test
    void rejectsAnUnsupportedFormatBeforeSynthesizing() {
        var service = new SoundRenderingService(synthesizer, new AudioEncoderRegistry(List.of()));

        assertThrows(IllegalArgumentException.class,
                () -> service.render(SCORE, InstrumentType.PIANO, AudioFormat.WAV, "interval-M3"));
        assertEquals(0, synthesizer.calls);
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn test -Dtest='AudioFormatTest,AudioEncoderRegistryTest,SoundRenderingServiceTest'`
Expected: BUILD FAILURE with `cannot find symbol` (`PcmAudio`, `AudioFormat`, `AudioEncoderRegistry`, …).

- [ ] **Step 3: Implement the payloads and ports**

`src/main/java/vn/ktt/music/application/sound/audio/AudioFormat.java`:

```java
package vn.ktt.music.application.sound.audio;

public enum AudioFormat {
    WAV;

    public static AudioFormat fromString(String format) {
        for (AudioFormat value : values()) {
            if (value.name().equalsIgnoreCase(format)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unsupported audio format: " + format);
    }
}
```

`src/main/java/vn/ktt/music/application/sound/audio/PcmAudio.java`:

```java
package vn.ktt.music.application.sound.audio;

public record PcmAudio(float[] samples, float sampleRate, int channels) {
}
```

`src/main/java/vn/ktt/music/application/sound/audio/EncodedAudio.java`:

```java
package vn.ktt.music.application.sound.audio;

public record EncodedAudio(byte[] data, String mimeType, String extension) {
}
```

`src/main/java/vn/ktt/music/application/sound/outbound/ISoundSynthesizerPort.java`:

```java
package vn.ktt.music.application.sound.outbound;

import vn.ktt.music.application.sound.audio.PcmAudio;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.domain.sound.valueobject.Score;

public interface ISoundSynthesizerPort {
    PcmAudio synthesize(Score score, InstrumentType instrument);
}
```

`src/main/java/vn/ktt/music/application/sound/outbound/IAudioEncoderPort.java`:

```java
package vn.ktt.music.application.sound.outbound;

import vn.ktt.music.application.sound.audio.AudioFormat;
import vn.ktt.music.application.sound.audio.EncodedAudio;
import vn.ktt.music.application.sound.audio.PcmAudio;

public interface IAudioEncoderPort {
    AudioFormat format();

    EncodedAudio encode(PcmAudio audio);
}
```

`src/main/java/vn/ktt/music/application/sound/AudioEncoderRegistry.java`:

```java
package vn.ktt.music.application.sound;

import org.springframework.stereotype.Component;
import vn.ktt.music.application.sound.audio.AudioFormat;
import vn.ktt.music.application.sound.outbound.IAudioEncoderPort;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// Not a vn.ktt.shared.ServiceRegistry: that one keys by class, and all AudioFormat constants share one class.
@Component
public class AudioEncoderRegistry {
    private final Map<AudioFormat, IAudioEncoderPort> encoders;

    public AudioEncoderRegistry(List<IAudioEncoderPort> encoders) {
        Map<AudioFormat, IAudioEncoderPort> byFormat = new EnumMap<>(AudioFormat.class);
        for (IAudioEncoderPort encoder : encoders) {
            if (byFormat.putIfAbsent(encoder.format(), encoder) != null) {
                throw new IllegalStateException("Duplicate audio encoder for format " + encoder.format());
            }
        }
        this.encoders = Collections.unmodifiableMap(byFormat);
    }

    public IAudioEncoderPort get(AudioFormat format) {
        IAudioEncoderPort encoder = encoders.get(format);
        if (encoder == null) {
            throw new IllegalArgumentException("Unsupported audio format: " + format);
        }
        return encoder;
    }

    public List<AudioFormat> supportedFormats() {
        return List.copyOf(encoders.keySet());
    }
}
```

Replace `src/main/java/vn/ktt/music/application/sound/dto/AudioContent.java` with:

```java
package vn.ktt.music.application.sound.dto;

public record AudioContent(byte[] data, String mimeType, String fileName) {
}
```

`src/main/java/vn/ktt/music/application/sound/SoundRenderingService.java`:

```java
package vn.ktt.music.application.sound;

import org.springframework.stereotype.Service;
import vn.ktt.music.application.sound.audio.AudioFormat;
import vn.ktt.music.application.sound.audio.EncodedAudio;
import vn.ktt.music.application.sound.audio.PcmAudio;
import vn.ktt.music.application.sound.dto.AudioContent;
import vn.ktt.music.application.sound.outbound.IAudioEncoderPort;
import vn.ktt.music.application.sound.outbound.ISoundSynthesizerPort;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.domain.sound.valueobject.Score;

@Service
public class SoundRenderingService {
    private final ISoundSynthesizerPort synthesizer;
    private final AudioEncoderRegistry encoderRegistry;

    public SoundRenderingService(ISoundSynthesizerPort synthesizer, AudioEncoderRegistry encoderRegistry) {
        this.synthesizer = synthesizer;
        this.encoderRegistry = encoderRegistry;
    }

    public AudioContent render(Score score, InstrumentType instrument, AudioFormat format, String baseName) {
        // Resolve the encoder first so an unsupported format fails before the expensive synthesis.
        IAudioEncoderPort encoder = encoderRegistry.get(format);
        PcmAudio pcm = synthesizer.synthesize(score, instrument);
        EncodedAudio encoded = encoder.encode(pcm);
        return new AudioContent(encoded.data(), encoded.mimeType(), baseName + "." + encoded.extension());
    }
}
```

- [ ] **Step 4: Keep the old path compiling against the `AudioContent` record**

In `src/main/java/vn/ktt/music/application/sound/IntervalGeneratorService.java`, replace the `toAudioContent` method with:

```java
    private AudioContent toAudioContent(byte[] data, String fileName) {
        return new AudioContent(data, "audio/wav", fileName);
    }
```

In `src/main/java/vn/ktt/music/infrastructure/controller/IntervalRangeController.java`, replace `audio.getFileName()` with `audio.fileName()` and `audio.getData()` with `audio.data()`.

In `src/main/java/vn/ktt/music/infrastructure/controller/IntervalsController.java`, replace `intervalAudioContent.getFileName()` with `intervalAudioContent.fileName()` and `intervalAudioContent.getData()` with `intervalAudioContent.data()`.

- [ ] **Step 5: Run the tests to verify they pass**

Run: `mvn test -Dtest='AudioFormatTest,AudioEncoderRegistryTest,SoundRenderingServiceTest'`
Expected: BUILD SUCCESS, with 12 tests passing (6 parameterized `AudioFormatTest` invocations, 4 registry tests and 2 rendering tests).

Run: `mvn test`
Expected: BUILD SUCCESS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/vn/ktt/music/application/sound src/main/java/vn/ktt/music/infrastructure/controller \
        src/test/java/vn/ktt/music/application
git commit -F - <<'EOF'
feat(music): add technology-agnostic synthesizer and encoder ports

Adds AudioEncoderRegistry keyed by AudioFormat and a SoundRenderingService
that synthesizes a Score and encodes it. AudioContent becomes a record
that carries the MIME type.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
EOF
```

---

### Task 4: Renderers emit PcmAudio, AudioGenerationException, SF2 header fix

**Files:**
- Create: `src/main/java/vn/ktt/music/infrastructure/audio/AudioGenerationException.java`
- Modify: `src/main/java/vn/ktt/music/infrastructure/audio/renderer/IMidiRenderer.java`
- Modify: `src/main/java/vn/ktt/music/infrastructure/audio/renderer/HarmonicMidiRenderer.java` (lines 33–52)
- Modify: `src/main/java/vn/ktt/music/infrastructure/audio/renderer/Sf2BasedMidiRenderer.java` (the imports, lines 63–107, and a new static method)
- Delete: `src/main/java/vn/ktt/music/infrastructure/audio/renderer/PcmSamples.java`
- Modify: `src/main/java/vn/ktt/music/infrastructure/audio/MidiSoundGenerator.java` (types only; this is temporary, and Task 9 deletes the file)
- Modify: `src/main/java/vn/ktt/music/infrastructure/audio/encoder/WavEncoder.java` (types only; this is temporary, and Task 5 deletes the file)
- Test: `src/test/java/vn/ktt/music/infrastructure/audio/renderer/Sf2BasedMidiRendererTest.java`

**Interfaces:**
- Consumes: Task 3's `PcmAudio`.
- Produces:
  - `class AudioGenerationException extends RuntimeException`, with the constructor `(String message, Throwable cause)`
  - `IMidiRenderer.render(Sequence)` now returns `PcmAudio`
  - The package-private `static float[] Sf2BasedMidiRenderer.decodePcm16Le(byte[] pcm, int frames)`

- [ ] **Step 1: Write the failing test**

`src/test/java/vn/ktt/music/infrastructure/audio/renderer/Sf2BasedMidiRendererTest.java`:

```java
package vn.ktt.music.infrastructure.audio.renderer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Sf2BasedMidiRendererTest {

    @Test
    void decodesSigned16BitLittleEndianSamples() {
        byte[] pcm = {0x00, 0x40, (byte) 0xFF, 0x7F, 0x00, (byte) 0x80, 0x00, 0x00};

        float[] samples = Sf2BasedMidiRenderer.decodePcm16Le(pcm, 4);

        assertArrayEquals(new float[]{0.5f, 32767 / 32768f, -1.0f, 0f}, samples);
    }

    @Test
    void decodesOnlyTheRequestedFrames() {
        byte[] pcm = {0x00, 0x40, 0x00, 0x40};

        assertEquals(1, Sf2BasedMidiRenderer.decodePcm16Le(pcm, 1).length);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `mvn test -Dtest=Sf2BasedMidiRendererTest`
Expected: BUILD FAILURE with `cannot find symbol: method decodePcm16Le`.

- [ ] **Step 3: Add the exception and switch the renderer port to `PcmAudio`**

`src/main/java/vn/ktt/music/infrastructure/audio/AudioGenerationException.java`:

```java
package vn.ktt.music.infrastructure.audio;

// Infrastructure failure while rendering or encoding audio. Deliberately not an IllegalStateException,
// which the planned controller advice maps to 409 Conflict.
public class AudioGenerationException extends RuntimeException {
    public AudioGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

Replace `src/main/java/vn/ktt/music/infrastructure/audio/renderer/IMidiRenderer.java` with:

```java
package vn.ktt.music.infrastructure.audio.renderer;

import vn.ktt.music.application.sound.audio.PcmAudio;

import javax.sound.midi.Sequence;

public interface IMidiRenderer {

    PcmAudio render(Sequence sequence);
}
```

Delete `PcmSamples.java`:

```bash
git rm src/main/java/vn/ktt/music/infrastructure/audio/renderer/PcmSamples.java
```

In `HarmonicMidiRenderer.java`, make these changes:
- Add the imports `vn.ktt.music.application.sound.audio.PcmAudio` and `vn.ktt.music.infrastructure.audio.AudioGenerationException`.
- Replace the `render` method (lines 33–52) with:

```java
    @Override
    public PcmAudio render(Sequence sequence) {
        try {
            float secondsPerTick = tempoSecondsPerTick(sequence);
            List<RenderedNote> renderedNotes = collectNotes(sequence, secondsPerTick);
            if (renderedNotes.isEmpty()) {
                return new PcmAudio(new float[0], SAMPLE_RATE_HZ, CHANNELS);
            }

            int totalSamples = renderedNotes.stream()
                    .mapToInt(RenderedNote::endSample)
                    .max().orElse(0) + 1;
            float[] samples = new float[totalSamples];
            for (RenderedNote renderedNote : renderedNotes) {
                renderNote(renderedNote, samples);
            }
            return new PcmAudio(samples, SAMPLE_RATE_HZ, CHANNELS);
        } catch (Exception e) {
            throw new AudioGenerationException("Failed to render MIDI sequence", e);
        }
    }
```

- [ ] **Step 4: Fix the SF2 renderer and extract `decodePcm16Le`**

In `Sf2BasedMidiRenderer.java`:
- Remove the imports `javax.sound.sampled.AudioFileFormat`, `javax.sound.sampled.AudioSystem` and `java.io.ByteArrayOutputStream`.
- Add the imports `vn.ktt.music.application.sound.audio.PcmAudio` and `vn.ktt.music.infrastructure.audio.AudioGenerationException`.
- Replace the `render` method (lines 63–107) with the following. Only the block after `send(...)` and the `catch` change: the stream is now read as raw PCM instead of being written as a WAVE file and decoded from byte 0.

```java
    @Override
    public synchronized PcmAudio render(Sequence sequence) {
        try (Synthesizer synthesizer = MidiSystem.getSynthesizer()) {
            Soundbank soundbank = loadSoundbank();
            AudioFormat format = new AudioFormat(SAMPLE_RATE, BITS_PER_SAMPLE, CHANNELS, true, false);

            Map<String, Object> properties = new HashMap<>();
            properties.put("interpolation", "linear");

            AudioInputStream stream = openStream(synthesizer, format, properties);
            try (Receiver receiver = synthesizer.getReceiver()) {
                Soundbank defaultSoundbank = synthesizer.getDefaultSoundbank();
                if (defaultSoundbank != null) {
                    synthesizer.unloadAllInstruments(defaultSoundbank);
                }
                synthesizer.loadAllInstruments(soundbank);

                double durationSeconds = send(sequence, receiver);
                int frameLength = (int) (format.getFrameRate() * (durationSeconds + TAIL_SECONDS));
                // Read raw PCM frames. Writing a WAVE container first put its 44-byte header into the samples.
                byte[] pcm = stream.readNBytes(frameLength * format.getFrameSize());

                int frameCount = pcm.length / format.getFrameSize();
                float[] samples = decodePcm16Le(pcm, frameCount);
                int fadeSamples = (int) (SAMPLE_RATE * 0.05);
                int fadeLimit = Math.min(fadeSamples, frameCount);
                for (int i = 0; i < fadeLimit; i++) {
                    samples[i] *= (float) i / fadeSamples;
                }
                log.debug("Rendered {} frames ({}s) from sequence with soundfont {}", frameCount, durationSeconds, soundfontPath);
                return new PcmAudio(samples, SAMPLE_RATE, CHANNELS);
            } finally {
                stream.close();
            }
        } catch (Exception e) {
            throw new AudioGenerationException("Failed to render MIDI sequence with soundfont", e);
        }
    }

    static float[] decodePcm16Le(byte[] pcm, int frames) {
        float[] samples = new float[frames];
        for (int i = 0; i < frames; i++) {
            short value = (short) ((pcm[2 * i] & 0xff) | (pcm[2 * i + 1] << 8));
            samples[i] = value / 32768.0f;
        }
        return samples;
    }
```

Leave the startup `IllegalStateException` in `resolveOpenStreamMethod` unchanged.

- [ ] **Step 5: Keep the old generator and encoder compiling**

In `src/main/java/vn/ktt/music/infrastructure/audio/MidiSoundGenerator.java`:
- Replace `import vn.ktt.music.infrastructure.audio.renderer.PcmSamples;` with `import vn.ktt.music.application.sound.audio.PcmAudio;`.
- Replace both occurrences of `PcmSamples samples` with `PcmAudio samples`.

In `src/main/java/vn/ktt/music/infrastructure/audio/encoder/WavEncoder.java`:
- Replace `import vn.ktt.music.infrastructure.audio.renderer.PcmSamples;` with `import vn.ktt.music.application.sound.audio.PcmAudio;`.
- Change the signature to `public byte[] encode(PcmAudio audio)`.
- Change the throw on line 35 to `throw new AudioGenerationException("Failed to encode WAV", e);`, and add `import vn.ktt.music.infrastructure.audio.AudioGenerationException;`.

- [ ] **Step 6: Run the tests**

Run: `mvn test -Dtest=Sf2BasedMidiRendererTest`
Expected: BUILD SUCCESS, with 2 tests passing.

Run: `mvn test`
Expected: BUILD SUCCESS.

Run: `grep -rn "PcmSamples" src/`
Expected: no output.

- [ ] **Step 7: Commit**

```bash
git add -A src/main/java/vn/ktt/music/infrastructure/audio src/test/java/vn/ktt/music/infrastructure
git commit -F - <<'EOF'
fix(music): read raw PCM from the SF2 synthesizer stream

The renderer wrote a WAVE container and decoded it from byte 0, so the
44-byte header became ~22 garbage samples hidden by the fade-in.
Renderers now return the application PcmAudio type and raise
AudioGenerationException on failure.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
EOF
```

---

### Task 5: MIDI synthesizer adapter and WAV encoder adapter

**Files:**
- Create: `src/main/java/vn/ktt/music/infrastructure/audio/midi/ScoreToMidiSequenceConverter.java`
- Create: `src/main/java/vn/ktt/music/infrastructure/audio/MidiSoundSynthesizer.java`
- Create: `src/main/java/vn/ktt/music/infrastructure/audio/encoder/WavAudioEncoder.java`
- Delete: `src/main/java/vn/ktt/music/infrastructure/audio/encoder/WavEncoder.java`
- Modify: `src/main/java/vn/ktt/music/infrastructure/audio/MidiSoundGenerator.java` (use `WavAudioEncoder`; temporary)
- Test: `src/test/java/vn/ktt/music/infrastructure/audio/midi/ScoreToMidiSequenceConverterTest.java`
- Test: `src/test/java/vn/ktt/music/infrastructure/audio/encoder/WavAudioEncoderTest.java`
- Test: `src/test/java/vn/ktt/music/infrastructure/audio/MidiSoundSynthesizerTest.java`

**Interfaces:**
- Consumes:
  - Task 1: `Score`, `NoteEvent`.
  - Task 3: `ISoundSynthesizerPort`, `IAudioEncoderPort`, `PcmAudio`, `EncodedAudio`, `AudioFormat`.
  - Task 4: `IMidiRenderer` (returns `PcmAudio`), `HarmonicMidiRenderer` (no-arg constructor), `AudioGenerationException`.
- Produces:
  - `@Component ScoreToMidiSequenceConverter`, with:
    - `Sequence convert(Score score, InstrumentType instrument)`
    - package-private `static final int PPQ = 500`
    - package-private `static int toVelocity(int loudness)`
  - `@Component MidiSoundSynthesizer(ScoreToMidiSequenceConverter, IMidiRenderer) implements ISoundSynthesizerPort`
  - `@Component WavAudioEncoder implements IAudioEncoderPort`, with format `WAV`, MIME type `audio/wav` and extension `wav`

- [ ] **Step 1: Write the failing tests**

`src/test/java/vn/ktt/music/infrastructure/audio/midi/ScoreToMidiSequenceConverterTest.java`:

```java
package vn.ktt.music.infrastructure.audio.midi;

import org.junit.jupiter.api.Test;
import vn.ktt.music.domain.atom.Pitch;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.domain.sound.valueobject.NoteEvent;
import vn.ktt.music.domain.sound.valueobject.Score;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreToMidiSequenceConverterTest {

    private final ScoreToMidiSequenceConverter converter = new ScoreToMidiSequenceConverter();

    private static List<MidiEvent> shortMessages(Track track) {
        List<MidiEvent> events = new ArrayList<>();
        for (int i = 0; i < track.size(); i++) {
            if (track.get(i).getMessage() instanceof ShortMessage) {
                events.add(track.get(i));
            }
        }
        return events;
    }

    @Test
    void usesOneTickPerMillisecondOnASingleTrack() {
        Sequence sequence = converter.convert(
                new Score(List.of(new NoteEvent(Pitch.convertFromMidiNumber(60), 63, 188, 71))), InstrumentType.PIANO);

        assertEquals(Sequence.PPQ, sequence.getDivisionType());
        assertEquals(500, sequence.getResolution());
        assertEquals(1, sequence.getTracks().length);
    }

    @Test
    void writesProgramChangeThenNoteOnAndOffAtMillisecondTicks() {
        Sequence sequence = converter.convert(
                new Score(List.of(new NoteEvent(Pitch.convertFromMidiNumber(60), 63, 188, 71))), InstrumentType.PIANO);

        List<MidiEvent> events = shortMessages(sequence.getTracks()[0]);
        assertEquals(3, events.size());

        ShortMessage program = (ShortMessage) events.get(0).getMessage();
        assertEquals(ShortMessage.PROGRAM_CHANGE, program.getCommand());
        assertEquals(0, program.getData1());
        assertEquals(0, events.get(0).getTick());

        ShortMessage noteOn = (ShortMessage) events.get(1).getMessage();
        assertEquals(ShortMessage.NOTE_ON, noteOn.getCommand());
        assertEquals(60, noteOn.getData1());
        assertEquals(90, noteOn.getData2());
        assertEquals(63, events.get(1).getTick());

        ShortMessage noteOff = (ShortMessage) events.get(2).getMessage();
        assertEquals(ShortMessage.NOTE_OFF, noteOff.getCommand());
        assertEquals(60, noteOff.getData1());
        assertEquals(251, events.get(2).getTick());
    }

    @Test
    void mapsLoudnessOntoMidiVelocity() {
        assertEquals(90, ScoreToMidiSequenceConverter.toVelocity(71));
        assertEquals(127, ScoreToMidiSequenceConverter.toVelocity(100));
        assertEquals(1, ScoreToMidiSequenceConverter.toVelocity(1));
    }
}
```

`src/test/java/vn/ktt/music/infrastructure/audio/encoder/WavAudioEncoderTest.java`:

```java
package vn.ktt.music.infrastructure.audio.encoder;

import org.junit.jupiter.api.Test;
import vn.ktt.music.application.sound.audio.AudioFormat;
import vn.ktt.music.application.sound.audio.EncodedAudio;
import vn.ktt.music.application.sound.audio.PcmAudio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WavAudioEncoderTest {

    private final WavAudioEncoder encoder = new WavAudioEncoder();

    @Test
    void declaresWav() {
        assertEquals(AudioFormat.WAV, encoder.format());
    }

    @Test
    void encodesAReadable16BitWaveFile() throws Exception {
        EncodedAudio encoded = encoder.encode(new PcmAudio(new float[]{0f, 0.5f, -1f}, 44100f, 1));

        assertEquals("audio/wav", encoded.mimeType());
        assertEquals("wav", encoded.extension());
        assertEquals("RIFF", new String(encoded.data(), 0, 4, StandardCharsets.US_ASCII));
        assertEquals("WAVE", new String(encoded.data(), 8, 4, StandardCharsets.US_ASCII));
        try (AudioInputStream in = AudioSystem.getAudioInputStream(new ByteArrayInputStream(encoded.data()))) {
            assertEquals(3, in.getFrameLength());
            assertEquals(16, in.getFormat().getSampleSizeInBits());
            assertEquals(44100f, in.getFormat().getSampleRate());
        }
    }
}
```

`src/test/java/vn/ktt/music/infrastructure/audio/MidiSoundSynthesizerTest.java`:

```java
package vn.ktt.music.infrastructure.audio;

import org.junit.jupiter.api.Test;
import vn.ktt.music.application.sound.audio.PcmAudio;
import vn.ktt.music.domain.atom.Pitch;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.domain.sound.valueobject.NoteEvent;
import vn.ktt.music.domain.sound.valueobject.Score;
import vn.ktt.music.infrastructure.audio.midi.ScoreToMidiSequenceConverter;
import vn.ktt.music.infrastructure.audio.renderer.HarmonicMidiRenderer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Uses the oscillator renderer, which needs no soundfont, to check timing end to end.
class MidiSoundSynthesizerTest {

    private final MidiSoundSynthesizer synthesizer =
            new MidiSoundSynthesizer(new ScoreToMidiSequenceConverter(), new HarmonicMidiRenderer());

    @Test
    void oneMillisecondOfScoreIsOneMillisecondOfAudio() {
        Score score = new Score(List.of(new NoteEvent(Pitch.convertFromMidiNumber(69), 0, 1000, 71)));

        PcmAudio pcm = synthesizer.synthesize(score, InstrumentType.PIANO);

        assertEquals(44100f, pcm.sampleRate());
        assertEquals(44101, pcm.samples().length, 1);
    }

    @Test
    void unisonStackedNotesRender() {
        Pitch c4 = Pitch.convertFromMidiNumber(60);
        Score score = new Score(List.of(new NoteEvent(c4, 63, 376, 71), new NoteEvent(c4, 63, 376, 71)));

        PcmAudio pcm = synthesizer.synthesize(score, InstrumentType.PIANO);

        assertTrue(pcm.samples().length > 0);
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn test -Dtest='ScoreToMidiSequenceConverterTest,WavAudioEncoderTest,MidiSoundSynthesizerTest'`
Expected: BUILD FAILURE with `cannot find symbol` (`ScoreToMidiSequenceConverter`, `WavAudioEncoder`, `MidiSoundSynthesizer`).

- [ ] **Step 3: Implement the converter and synthesizer**

`src/main/java/vn/ktt/music/infrastructure/audio/midi/ScoreToMidiSequenceConverter.java`:

```java
package vn.ktt.music.infrastructure.audio.midi;

import org.springframework.stereotype.Component;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.domain.sound.valueobject.NoteEvent;
import vn.ktt.music.domain.sound.valueobject.Score;
import vn.ktt.music.infrastructure.audio.AudioGenerationException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

@Component
public class ScoreToMidiSequenceConverter {

    // At the default 500,000 µs per quarter note, 500 ticks per quarter makes one tick exactly one millisecond.
    // No tempo meta event is written: HarmonicMidiRenderer only reads tempo from the first track.
    static final int PPQ = 500;
    private static final int CHANNEL = 0;

    public Sequence convert(Score score, InstrumentType instrument) {
        try {
            Sequence sequence = new Sequence(Sequence.PPQ, PPQ);
            Track track = sequence.createTrack();
            track.add(new MidiEvent(
                    new ShortMessage(ShortMessage.PROGRAM_CHANGE, CHANNEL, programOf(instrument), 0), 0));
            for (NoteEvent note : score.events()) {
                int midiNote = note.pitch().toMidiNumber();
                track.add(new MidiEvent(
                        new ShortMessage(ShortMessage.NOTE_ON, CHANNEL, midiNote, toVelocity(note.loudness())),
                        note.onsetMs()));
                track.add(new MidiEvent(
                        new ShortMessage(ShortMessage.NOTE_OFF, CHANNEL, midiNote, 0),
                        note.onsetMs() + note.durationMs()));
            }
            return sequence;
        } catch (InvalidMidiDataException e) {
            throw new AudioGenerationException("Failed to convert score to MIDI", e);
        }
    }

    static int toVelocity(int loudness) {
        return Math.round(loudness * 127 / 100f);
    }

    private static int programOf(InstrumentType instrument) {
        return switch (instrument) {
            case PIANO -> 0;
        };
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/audio/MidiSoundSynthesizer.java`:

```java
package vn.ktt.music.infrastructure.audio;

import org.springframework.stereotype.Component;
import vn.ktt.music.application.sound.audio.PcmAudio;
import vn.ktt.music.application.sound.outbound.ISoundSynthesizerPort;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.domain.sound.valueobject.Score;
import vn.ktt.music.infrastructure.audio.midi.ScoreToMidiSequenceConverter;
import vn.ktt.music.infrastructure.audio.renderer.IMidiRenderer;

@Component
public class MidiSoundSynthesizer implements ISoundSynthesizerPort {

    private final ScoreToMidiSequenceConverter converter;
    private final IMidiRenderer renderer;

    public MidiSoundSynthesizer(ScoreToMidiSequenceConverter converter, IMidiRenderer renderer) {
        this.converter = converter;
        this.renderer = renderer;
    }

    @Override
    public PcmAudio synthesize(Score score, InstrumentType instrument) {
        return renderer.render(converter.convert(score, instrument));
    }
}
```

- [ ] **Step 4: Replace `WavEncoder` with `WavAudioEncoder`**

`src/main/java/vn/ktt/music/infrastructure/audio/encoder/WavAudioEncoder.java`:

```java
package vn.ktt.music.infrastructure.audio.encoder;

import org.springframework.stereotype.Component;
import vn.ktt.music.application.sound.audio.AudioFormat;
import vn.ktt.music.application.sound.audio.EncodedAudio;
import vn.ktt.music.application.sound.audio.PcmAudio;
import vn.ktt.music.application.sound.outbound.IAudioEncoderPort;
import vn.ktt.music.infrastructure.audio.AudioGenerationException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Component
public class WavAudioEncoder implements IAudioEncoderPort {

    @Override
    public AudioFormat format() {
        return AudioFormat.WAV;
    }

    @Override
    public EncodedAudio encode(PcmAudio audio) {
        var format = new javax.sound.sampled.AudioFormat(audio.sampleRate(), 16, audio.channels(), true, false);
        int frames = audio.samples().length / audio.channels();
        byte[] pcm = new byte[audio.samples().length * 2];
        int sampleIndex = 0;
        for (int frame = 0; frame < frames; frame++) {
            for (int ch = 0; ch < audio.channels(); ch++) {
                float sample = Math.clamp(audio.samples()[sampleIndex++], -1f, 1f);
                short value = (short) (sample * Short.MAX_VALUE);
                int offset = frame * audio.channels() * 2 + ch * 2;
                pcm[offset] = (byte) (value & 0xff);
                pcm[offset + 1] = (byte) ((value >> 8) & 0xff);
            }
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(pcm), format, frames);
            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, out);
            return new EncodedAudio(out.toByteArray(), "audio/wav", "wav");
        } catch (Exception e) {
            throw new AudioGenerationException("Failed to encode WAV", e);
        }
    }
}
```

The fully qualified `javax.sound.sampled.AudioFormat` is intentional. It avoids clashing with the application's `AudioFormat` enum, which `format()` returns.

```bash
git rm src/main/java/vn/ktt/music/infrastructure/audio/encoder/WavEncoder.java
```

In `src/main/java/vn/ktt/music/infrastructure/audio/MidiSoundGenerator.java`:
- Replace `import vn.ktt.music.infrastructure.audio.encoder.WavEncoder;` with `import vn.ktt.music.infrastructure.audio.encoder.WavAudioEncoder;`.
- Change the field and the constructor parameter type from `WavEncoder` to `WavAudioEncoder`.
- Replace both `return encoder.encode(samples);` with `return encoder.encode(samples).data();`.

- [ ] **Step 5: Run the tests to verify they pass**

Run: `mvn test -Dtest='ScoreToMidiSequenceConverterTest,WavAudioEncoderTest,MidiSoundSynthesizerTest'`
Expected: BUILD SUCCESS, with 7 tests passing.

Run: `mvn test`
Expected: BUILD SUCCESS.

- [ ] **Step 6: Smoke-start the app (the context now wires `SoundRenderingService`)**

Prerequisites: `docker compose up -d`, and `src/main/resources/soundfonts/grand_piano.sf2` present.

Run: `mvn spring-boot:run` in the background, then `curl -s -o /tmp/p5.wav -w "%{http_code} %{content_type}\n" "http://localhost:8080/api/intervals/P5/random?texture=ascending"`
Expected: `200 audio/wav`. Stop the app afterwards.

If the SF2 file or Docker isn't available, skip this step. Note in the task report that it was skipped. Don't install anything.

- [ ] **Step 7: Commit**

```bash
git add -A src/main/java/vn/ktt/music/infrastructure/audio src/test/java/vn/ktt/music/infrastructure/audio
git commit -F - <<'EOF'
feat(music): add MIDI synthesizer and WAV encoder adapters

MIDI is now confined to MidiSoundSynthesizer (score -> 1 ms/tick
sequence -> IMidiRenderer), and WAV to WavAudioEncoder behind
IAudioEncoderPort.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
EOF
```

---

### Task 6: Persistence for sound settings and instruments

**Files:**
- Create: `src/main/java/vn/ktt/music/domain/sound/repository/ISoundSettingsRepository.java`
- Create: `src/main/java/vn/ktt/music/domain/instrument/repository/IInstrumentRepository.java`
- Modify: `src/main/java/vn/ktt/music/infrastructure/persistence/entity/MusicalConfigurationEntity.java`
- Move and rename: `src/main/java/vn/ktt/music/infrastructure/persistence/MusicalConfigurationRepository.java` becomes `src/main/java/vn/ktt/music/infrastructure/persistence/gateway/MusicalConfigurationJpaRepository.java`
- Create: `src/main/java/vn/ktt/music/infrastructure/persistence/gateway/InstrumentJpaRepository.java`
- Create: `src/main/java/vn/ktt/music/infrastructure/persistence/gateway/SoundSettingsRepository.java`
- Create: `src/main/java/vn/ktt/music/infrastructure/persistence/gateway/InstrumentRepository.java`
- Modify: `src/main/java/vn/ktt/music/infrastructure/adapter/MusicalConfigurationDataSourceAdapter.java` (only the renamed type; temporary)
- Modify: `src/main/resources/import.sql:7`
- Test: `src/test/java/vn/ktt/music/infrastructure/persistence/gateway/SoundSettingsRepositoryTest.java`
- Test: `src/test/java/vn/ktt/music/infrastructure/persistence/gateway/InstrumentRepositoryTest.java`

**Interfaces:**
- Consumes: Task 1's `SoundSettings`. Also the existing `Instrument.reconstruct(IMusicalEntityFactory, InstrumentType, String, String)`, `InstrumentEntity` (Lombok getters and setters: `instrumentType`, `lowestPitch`, `highestPitch`) and `IMusicalEntityFactory`.
- Produces:
  - `interface ISoundSettingsRepository { SoundSettings load(); void save(SoundSettings settings); }`
  - `interface IInstrumentRepository { Optional<Instrument> findByType(InstrumentType type); List<Instrument> findAll(); }`
  - `interface MusicalConfigurationJpaRepository extends JpaRepository<MusicalConfigurationEntity, UUID>`
  - `interface InstrumentJpaRepository extends JpaRepository<InstrumentEntity, UUID>`, with `Optional<InstrumentEntity> findByInstrumentType(InstrumentType)`
  - `@Repository SoundSettingsRepository(MusicalConfigurationJpaRepository, InstrumentJpaRepository)`
  - `@Repository InstrumentRepository(InstrumentJpaRepository, IMusicalEntityFactory)`

- [ ] **Step 1: Write the failing tests**

`src/test/java/vn/ktt/music/infrastructure/persistence/gateway/SoundSettingsRepositoryTest.java`:

```java
package vn.ktt.music.infrastructure.persistence.gateway;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.domain.sound.valueobject.SoundSettings;
import vn.ktt.music.infrastructure.persistence.entity.InstrumentEntity;
import vn.ktt.music.infrastructure.persistence.entity.MusicalConfigurationEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SoundSettingsRepositoryTest {

    private final MusicalConfigurationJpaRepository configurations = mock(MusicalConfigurationJpaRepository.class);
    private final InstrumentJpaRepository instruments = mock(InstrumentJpaRepository.class);
    private final SoundSettingsRepository repository = new SoundSettingsRepository(configurations, instruments);

    private static InstrumentEntity pianoEntity() {
        var piano = new InstrumentEntity();
        piano.setInstrumentType(InstrumentType.PIANO);
        piano.setLowestPitch("A0");
        piano.setHighestPitch("C8");
        return piano;
    }

    private static MusicalConfigurationEntity configuration(int noteDurationMs, int silenceMs, int loudness) {
        var configuration = new MusicalConfigurationEntity();
        configuration.setActiveInstrument(pianoEntity());
        configuration.setNoteDurationMs(noteDurationMs);
        configuration.setSilenceMs(silenceMs);
        configuration.setLoudness(loudness);
        return configuration;
    }

    @Test
    void emptyTableGivesDefaults() {
        when(configurations.findAll()).thenReturn(List.of());

        assertEquals(SoundSettings.defaults(), repository.load());
    }

    @Test
    void mapsTheSavedRow() {
        when(configurations.findAll()).thenReturn(List.of(configuration(300, 40, 55)));

        assertEquals(new SoundSettings(300, 40, 55, InstrumentType.PIANO), repository.load());
    }

    @Test
    void saveUpdatesTheExistingRow() {
        var existing = configuration(188, 21, 71);
        when(configurations.findAll()).thenReturn(List.of(existing));
        when(instruments.findByInstrumentType(InstrumentType.PIANO)).thenReturn(Optional.of(pianoEntity()));

        repository.save(new SoundSettings(300, 0, 90, InstrumentType.PIANO));

        ArgumentCaptor<MusicalConfigurationEntity> saved = ArgumentCaptor.forClass(MusicalConfigurationEntity.class);
        verify(configurations).save(saved.capture());
        assertSame(existing, saved.getValue());
        assertEquals(300, existing.getNoteDurationMs());
        assertEquals(0, existing.getSilenceMs());
        assertEquals(90, existing.getLoudness());
    }

    @Test
    void saveCreatesARowWhenTableIsEmpty() {
        when(configurations.findAll()).thenReturn(List.of());
        when(instruments.findByInstrumentType(InstrumentType.PIANO)).thenReturn(Optional.of(pianoEntity()));

        repository.save(SoundSettings.defaults());

        ArgumentCaptor<MusicalConfigurationEntity> saved = ArgumentCaptor.forClass(MusicalConfigurationEntity.class);
        verify(configurations).save(saved.capture());
        assertEquals(188, saved.getValue().getNoteDurationMs());
        assertEquals(InstrumentType.PIANO, saved.getValue().getActiveInstrument().getInstrumentType());
    }

    @Test
    void saveRejectsAnInstrumentWithoutRow() {
        when(instruments.findByInstrumentType(InstrumentType.PIANO)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> repository.save(SoundSettings.defaults()));
        verify(configurations, never()).save(any());
    }
}
```

`src/test/java/vn/ktt/music/infrastructure/persistence/gateway/InstrumentRepositoryTest.java`:

```java
package vn.ktt.music.infrastructure.persistence.gateway;

import org.junit.jupiter.api.Test;
import vn.ktt.music.domain.factory.MusicalEntityFactory;
import vn.ktt.music.domain.instrument.Instrument;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.infrastructure.persistence.entity.InstrumentEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InstrumentRepositoryTest {

    private final InstrumentJpaRepository instruments = mock(InstrumentJpaRepository.class);
    private final InstrumentRepository repository = new InstrumentRepository(instruments, new MusicalEntityFactory());

    private static InstrumentEntity pianoEntity() {
        var piano = new InstrumentEntity();
        piano.setInstrumentType(InstrumentType.PIANO);
        piano.setLowestPitch("A0");
        piano.setHighestPitch("C8");
        return piano;
    }

    @Test
    void mapsAnEntityToTheDomainInstrument() {
        when(instruments.findByInstrumentType(InstrumentType.PIANO)).thenReturn(Optional.of(pianoEntity()));

        Instrument piano = repository.findByType(InstrumentType.PIANO).orElseThrow();

        assertEquals(21, piano.getLowestPitch().toMidiNumber());
        assertEquals(108, piano.getHighestPitch().toMidiNumber());
    }

    @Test
    void returnsEmptyForAnUnconfiguredInstrument() {
        when(instruments.findByInstrumentType(InstrumentType.PIANO)).thenReturn(Optional.empty());

        assertTrue(repository.findByType(InstrumentType.PIANO).isEmpty());
    }

    @Test
    void listsAllInstruments() {
        when(instruments.findAll()).thenReturn(List.of(pianoEntity()));

        assertEquals(List.of(InstrumentType.PIANO),
                repository.findAll().stream().map(Instrument::getInstrumentType).toList());
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn test -Dtest='SoundSettingsRepositoryTest,InstrumentRepositoryTest'`
Expected: BUILD FAILURE with `cannot find symbol` (`MusicalConfigurationJpaRepository`, `setNoteDurationMs`, …).

- [ ] **Step 3: Add the domain repository interfaces**

`src/main/java/vn/ktt/music/domain/sound/repository/ISoundSettingsRepository.java`:

```java
package vn.ktt.music.domain.sound.repository;

import vn.ktt.music.domain.sound.valueobject.SoundSettings;

public interface ISoundSettingsRepository {
    SoundSettings load();

    void save(SoundSettings settings);
}
```

`src/main/java/vn/ktt/music/domain/instrument/repository/IInstrumentRepository.java`:

```java
package vn.ktt.music.domain.instrument.repository;

import vn.ktt.music.domain.instrument.Instrument;
import vn.ktt.music.domain.instrument.InstrumentType;

import java.util.List;
import java.util.Optional;

public interface IInstrumentRepository {
    Optional<Instrument> findByType(InstrumentType type);

    List<Instrument> findAll();
}
```

- [ ] **Step 4: Update the entity, JPA repositories and seed**

In `MusicalConfigurationEntity.java`, add after the `activeInstrument` field:

```java
    @Column(name = "note_duration_ms", nullable = false)
    private int noteDurationMs;

    @Column(name = "silence_ms", nullable = false)
    private int silenceMs;

    @Column(name = "loudness", nullable = false)
    private int loudness;
```

Move and rename the Spring Data interface:

```bash
git mv src/main/java/vn/ktt/music/infrastructure/persistence/MusicalConfigurationRepository.java \
       src/main/java/vn/ktt/music/infrastructure/persistence/gateway/MusicalConfigurationJpaRepository.java
```

Then replace its contents with:

```java
package vn.ktt.music.infrastructure.persistence.gateway;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.ktt.music.infrastructure.persistence.entity.MusicalConfigurationEntity;

import java.util.UUID;

public interface MusicalConfigurationJpaRepository extends JpaRepository<MusicalConfigurationEntity, UUID> {
}
```

`src/main/java/vn/ktt/music/infrastructure/persistence/gateway/InstrumentJpaRepository.java`:

```java
package vn.ktt.music.infrastructure.persistence.gateway;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.infrastructure.persistence.entity.InstrumentEntity;

import java.util.Optional;
import java.util.UUID;

public interface InstrumentJpaRepository extends JpaRepository<InstrumentEntity, UUID> {
    Optional<InstrumentEntity> findByInstrumentType(InstrumentType instrumentType);
}
```

In `MusicalConfigurationDataSourceAdapter.java`:
- Replace `import vn.ktt.music.infrastructure.persistence.MusicalConfigurationRepository;` with `import vn.ktt.music.infrastructure.persistence.gateway.MusicalConfigurationJpaRepository;`.
- Replace the two uses of the type `MusicalConfigurationRepository` (the field and the constructor parameter) with `MusicalConfigurationJpaRepository`.

In `src/main/resources/import.sql`, replace line 7 with this single line:

```sql
INSERT INTO musical_config (id, active_instrument_id, note_duration_ms, silence_ms, loudness) SELECT gen_random_uuid(), i.id, 188, 21, 71 FROM instruments i WHERE i.instrument_type = 'PIANO';
```

- [ ] **Step 5: Implement the gateways**

`src/main/java/vn/ktt/music/infrastructure/persistence/gateway/SoundSettingsRepository.java`:

```java
package vn.ktt.music.infrastructure.persistence.gateway;

import org.springframework.stereotype.Repository;
import vn.ktt.music.domain.sound.repository.ISoundSettingsRepository;
import vn.ktt.music.domain.sound.valueobject.SoundSettings;
import vn.ktt.music.infrastructure.persistence.entity.InstrumentEntity;
import vn.ktt.music.infrastructure.persistence.entity.MusicalConfigurationEntity;

import java.util.Optional;

@Repository
public class SoundSettingsRepository implements ISoundSettingsRepository {
    private final MusicalConfigurationJpaRepository configurationJpaRepository;
    private final InstrumentJpaRepository instrumentJpaRepository;

    public SoundSettingsRepository(MusicalConfigurationJpaRepository configurationJpaRepository,
                                   InstrumentJpaRepository instrumentJpaRepository) {
        this.configurationJpaRepository = configurationJpaRepository;
        this.instrumentJpaRepository = instrumentJpaRepository;
    }

    @Override
    public SoundSettings load() {
        return findConfiguration()
                .map(configuration -> new SoundSettings(
                        configuration.getNoteDurationMs(),
                        configuration.getSilenceMs(),
                        configuration.getLoudness(),
                        configuration.getActiveInstrument().getInstrumentType()))
                .orElseGet(SoundSettings::defaults);
    }

    @Override
    public void save(SoundSettings settings) {
        InstrumentEntity instrument = instrumentJpaRepository.findByInstrumentType(settings.instrument())
                .orElseThrow(() -> new IllegalArgumentException("Instrument is not configured: " + settings.instrument()));
        MusicalConfigurationEntity configuration = findConfiguration().orElseGet(MusicalConfigurationEntity::new);
        configuration.setActiveInstrument(instrument);
        configuration.setNoteDurationMs(settings.noteDurationMs());
        configuration.setSilenceMs(settings.silenceMs());
        configuration.setLoudness(settings.loudness());
        configurationJpaRepository.save(configuration);
    }

    private Optional<MusicalConfigurationEntity> findConfiguration() {
        return configurationJpaRepository.findAll().stream().findFirst();
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/persistence/gateway/InstrumentRepository.java`:

```java
package vn.ktt.music.infrastructure.persistence.gateway;

import org.springframework.stereotype.Repository;
import vn.ktt.music.domain.factory.IMusicalEntityFactory;
import vn.ktt.music.domain.instrument.Instrument;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.domain.instrument.repository.IInstrumentRepository;
import vn.ktt.music.infrastructure.persistence.entity.InstrumentEntity;

import java.util.List;
import java.util.Optional;

@Repository
public class InstrumentRepository implements IInstrumentRepository {
    private final InstrumentJpaRepository instrumentJpaRepository;
    private final IMusicalEntityFactory musicalEntityFactory;

    public InstrumentRepository(InstrumentJpaRepository instrumentJpaRepository,
                                IMusicalEntityFactory musicalEntityFactory) {
        this.instrumentJpaRepository = instrumentJpaRepository;
        this.musicalEntityFactory = musicalEntityFactory;
    }

    @Override
    public Optional<Instrument> findByType(InstrumentType type) {
        return instrumentJpaRepository.findByInstrumentType(type).map(this::toDomain);
    }

    @Override
    public List<Instrument> findAll() {
        return instrumentJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    private Instrument toDomain(InstrumentEntity entity) {
        return Instrument.reconstruct(musicalEntityFactory, entity.getInstrumentType(),
                entity.getLowestPitch(), entity.getHighestPitch());
    }
}
```

- [ ] **Step 6: Run the tests to verify they pass**

Run: `mvn test -Dtest='SoundSettingsRepositoryTest,InstrumentRepositoryTest'`
Expected: BUILD SUCCESS, with 8 tests passing. A Mockito "dynamic agent" warning on JDK 25 is expected and harmless.

Run: `mvn test`
Expected: BUILD SUCCESS.

Run: `grep -c "" src/main/resources/import.sql && grep -n "musical_config" src/main/resources/import.sql`
Expected: the line count is unchanged from before the edit, and one `musical_config` line contains `188, 21, 71`.

- [ ] **Step 7: Commit**

```bash
git add -A src/main/java/vn/ktt/music/domain src/main/java/vn/ktt/music/infrastructure/persistence \
        src/main/java/vn/ktt/music/infrastructure/adapter src/main/resources/import.sql \
        src/test/java/vn/ktt/music/infrastructure/persistence
git commit -F - <<'EOF'
feat(music): persist sound settings in musical_config

Adds domain repositories for sound settings and instruments, implemented
by gateways over *JpaRepository, following the eartraining convention.
An empty table now loads the built-in defaults instead of failing.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
EOF
```

---

### Task 7: Use cases (UC-1 to UC-4)

**Files:**
- Create: `src/main/java/vn/ktt/music/application/settings/dto/SoundSettingsPatch.java`
- Create: `src/main/java/vn/ktt/music/application/settings/dto/SoundSettingsDTO.java`
- Create: `src/main/java/vn/ktt/music/application/settings/inbound/ISoundSettingsQueryPort.java`
- Create: `src/main/java/vn/ktt/music/application/settings/inbound/IUpdateSoundSettingsPort.java`
- Create: `src/main/java/vn/ktt/music/application/settings/SoundSettingsDTOAssembler.java`
- Create: `src/main/java/vn/ktt/music/application/settings/SoundSettingsQueryUseCase.java`
- Create: `src/main/java/vn/ktt/music/application/settings/UpdateSoundSettingsUseCase.java`
- Create: `src/main/java/vn/ktt/music/application/sound/dto/IntervalSoundCommand.java`
- Create: `src/main/java/vn/ktt/music/application/sound/dto/IntervalRangeSoundCommand.java`
- Create: `src/main/java/vn/ktt/music/application/sound/inbound/IIntervalSoundPort.java`
- Create: `src/main/java/vn/ktt/music/application/sound/IntervalSoundUseCase.java`
- Test support: `src/test/java/vn/ktt/music/application/support/FakeSoundSettingsRepository.java`, `FakeInstrumentRepository.java`, `RecordingScoreComposer.java`
- Test: `src/test/java/vn/ktt/music/application/settings/dto/SoundSettingsPatchTest.java`
- Test: `src/test/java/vn/ktt/music/application/sound/IntervalSoundUseCaseTest.java`
- Test: `src/test/java/vn/ktt/music/application/settings/SoundSettingsQueryUseCaseTest.java`
- Test: `src/test/java/vn/ktt/music/application/settings/UpdateSoundSettingsUseCaseTest.java`

**Interfaces:**
- Consumes:
  - Task 1: `SoundSettings`, `Direction`, `InstrumentType.fromString`, `Score`, `NoteEvent`.
  - Task 2: `IIntervalScoreComposer`.
  - Task 3: `SoundRenderingService`, `AudioEncoderRegistry`, `AudioFormat`, `AudioContent`, and the test fakes.
  - Task 6: `ISoundSettingsRepository`, `IInstrumentRepository`.
  - Existing: `IMusicalEntityFactory.getInterval(String)`, `IMusicalOperation.getLowerBoundPitchFromInterval(Pitch, IntervalType)` / `getRandomPitch(Pitch, Pitch)`, `Interval.Texture.fromString(String)`.
- Produces:
  - `record SoundSettingsPatch(Integer noteDurationMs, Integer silenceMs, Integer loudness, String instrument)`, with `static final SoundSettingsPatch EMPTY` and `SoundSettings applyTo(SoundSettings base)`
  - `record SoundSettingsDTO(int noteDurationMs, int silenceMs, int loudness, String instrument, List<String> availableInstruments, List<String> availableFormats)`
  - `record IntervalSoundCommand(String interval, String texture, SoundSettingsPatch overrides, String format)`
  - `record IntervalRangeSoundCommand(String interval, String texture, String direction, SoundSettingsPatch overrides, String format)`
  - `interface IIntervalSoundPort { AudioContent generateInterval(IntervalSoundCommand); AudioContent generateIntervalRange(IntervalRangeSoundCommand); }`
  - `interface ISoundSettingsQueryPort { SoundSettingsDTO getSettings(); }`
  - `interface IUpdateSoundSettingsPort { SoundSettingsDTO update(SoundSettingsPatch patch); }`
  - `@Service IntervalSoundUseCase(IMusicalEntityFactory, IMusicalOperation, IIntervalScoreComposer, ISoundSettingsRepository, IInstrumentRepository, SoundRenderingService)`
  - `@Component SoundSettingsDTOAssembler(IInstrumentRepository, AudioEncoderRegistry)`, with `SoundSettingsDTO toDTO(SoundSettings)`
  - `@Service SoundSettingsQueryUseCase(ISoundSettingsRepository, SoundSettingsDTOAssembler)`
  - `@Service UpdateSoundSettingsUseCase(ISoundSettingsRepository, IInstrumentRepository, SoundSettingsDTOAssembler)`

- [ ] **Step 1: Write the test fakes**

`src/test/java/vn/ktt/music/application/support/FakeSoundSettingsRepository.java`:

```java
package vn.ktt.music.application.support;

import vn.ktt.music.domain.sound.repository.ISoundSettingsRepository;
import vn.ktt.music.domain.sound.valueobject.SoundSettings;

public class FakeSoundSettingsRepository implements ISoundSettingsRepository {
    public SoundSettings stored;
    public int saves;

    public FakeSoundSettingsRepository(SoundSettings initial) {
        this.stored = initial;
    }

    @Override
    public SoundSettings load() {
        return stored;
    }

    @Override
    public void save(SoundSettings settings) {
        stored = settings;
        saves++;
    }
}
```

`src/test/java/vn/ktt/music/application/support/FakeInstrumentRepository.java`:

```java
package vn.ktt.music.application.support;

import vn.ktt.music.domain.factory.MusicalEntityFactory;
import vn.ktt.music.domain.instrument.Instrument;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.domain.instrument.repository.IInstrumentRepository;

import java.util.List;
import java.util.Optional;

public class FakeInstrumentRepository implements IInstrumentRepository {
    private final List<Instrument> instruments;

    public FakeInstrumentRepository(Instrument... instruments) {
        this.instruments = List.of(instruments);
    }

    public static Instrument piano(String lowest, String highest) {
        return Instrument.reconstruct(new MusicalEntityFactory(), InstrumentType.PIANO, lowest, highest);
    }

    public static Instrument piano() {
        return piano("A0", "C8");
    }

    @Override
    public Optional<Instrument> findByType(InstrumentType type) {
        return instruments.stream().filter(instrument -> instrument.getInstrumentType() == type).findFirst();
    }

    @Override
    public List<Instrument> findAll() {
        return instruments;
    }
}
```

`src/test/java/vn/ktt/music/application/support/RecordingScoreComposer.java`:

```java
package vn.ktt.music.application.support;

import vn.ktt.music.domain.atom.Pitch;
import vn.ktt.music.domain.composition.Interval;
import vn.ktt.music.domain.instrument.Instrument;
import vn.ktt.music.domain.service.IIntervalScoreComposer;
import vn.ktt.music.domain.sound.valueobject.Direction;
import vn.ktt.music.domain.sound.valueobject.NoteEvent;
import vn.ktt.music.domain.sound.valueobject.Score;
import vn.ktt.music.domain.sound.valueobject.SoundSettings;

import java.util.List;

public class RecordingScoreComposer implements IIntervalScoreComposer {
    public Interval lastInterval;
    public Interval.Texture lastTexture;
    public Pitch lastStart;
    public Direction lastDirection;
    public SoundSettings lastSettings;

    @Override
    public Score single(Interval interval, Interval.Texture texture, Pitch start, SoundSettings settings) {
        lastInterval = interval;
        lastTexture = texture;
        lastStart = start;
        lastSettings = settings;
        return oneNote();
    }

    @Override
    public Score range(Interval interval, Interval.Texture texture, Direction direction, Instrument instrument,
                       SoundSettings settings) {
        lastInterval = interval;
        lastTexture = texture;
        lastDirection = direction;
        lastSettings = settings;
        return oneNote();
    }

    private static Score oneNote() {
        return new Score(List.of(new NoteEvent(Pitch.convertFromMidiNumber(60), 0, 100, 50)));
    }
}
```

- [ ] **Step 2: Write the failing tests**

`src/test/java/vn/ktt/music/application/settings/dto/SoundSettingsPatchTest.java`:

```java
package vn.ktt.music.application.settings.dto;

import org.junit.jupiter.api.Test;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.domain.sound.valueobject.SoundSettings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SoundSettingsPatchTest {

    @Test
    void emptyPatchKeepsTheBase() {
        assertEquals(SoundSettings.defaults(), SoundSettingsPatch.EMPTY.applyTo(SoundSettings.defaults()));
    }

    @Test
    void appliesPresentFieldsAndParsesTheInstrumentInAnyCase() {
        var patch = new SoundSettingsPatch(300, null, 50, "piano");

        assertEquals(new SoundSettings(300, 21, 50, InstrumentType.PIANO), patch.applyTo(SoundSettings.defaults()));
    }

    @Test
    void rejectsAnUnknownInstrument() {
        var patch = new SoundSettingsPatch(null, null, null, "violin");

        assertThrows(IllegalArgumentException.class, () -> patch.applyTo(SoundSettings.defaults()));
    }
}
```

`src/test/java/vn/ktt/music/application/sound/IntervalSoundUseCaseTest.java`:

```java
package vn.ktt.music.application.sound;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import vn.ktt.music.application.settings.dto.SoundSettingsPatch;
import vn.ktt.music.application.sound.audio.AudioFormat;
import vn.ktt.music.application.sound.dto.AudioContent;
import vn.ktt.music.application.sound.dto.IntervalRangeSoundCommand;
import vn.ktt.music.application.sound.dto.IntervalSoundCommand;
import vn.ktt.music.application.support.FakeAudioEncoder;
import vn.ktt.music.application.support.FakeInstrumentRepository;
import vn.ktt.music.application.support.FakeSoundSettingsRepository;
import vn.ktt.music.application.support.FakeSoundSynthesizer;
import vn.ktt.music.application.support.RecordingScoreComposer;
import vn.ktt.music.domain.composition.Interval;
import vn.ktt.music.domain.factory.MusicalEntityFactory;
import vn.ktt.music.domain.instrument.Instrument;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.domain.service.MusicalOperation;
import vn.ktt.music.domain.sound.valueobject.Direction;
import vn.ktt.music.domain.sound.valueobject.SoundSettings;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntervalSoundUseCaseTest {

    private final FakeSoundSettingsRepository settingsRepository = new FakeSoundSettingsRepository(SoundSettings.defaults());
    private final RecordingScoreComposer composer = new RecordingScoreComposer();
    private final FakeSoundSynthesizer synthesizer = new FakeSoundSynthesizer();

    private IntervalSoundUseCase useCaseWith(Instrument... instruments) {
        var rendering = new SoundRenderingService(synthesizer,
                new AudioEncoderRegistry(List.of(new FakeAudioEncoder(AudioFormat.WAV))));
        return new IntervalSoundUseCase(new MusicalEntityFactory(), new MusicalOperation(), composer,
                settingsRepository, new FakeInstrumentRepository(instruments), rendering);
    }

    private final IntervalSoundUseCase useCase = useCaseWith(FakeInstrumentRepository.piano());

    @Test
    void generateIntervalAppliesOverridesOnTopOfSavedSettings() {
        settingsRepository.stored = new SoundSettings(200, 30, 60, InstrumentType.PIANO);

        useCase.generateInterval(new IntervalSoundCommand("M3", "ASCENDING",
                new SoundSettingsPatch(300, null, 50, null), "wav"));

        assertEquals(new SoundSettings(300, 30, 50, InstrumentType.PIANO), composer.lastSettings);
        assertEquals(Interval.Texture.ASCENDING, composer.lastTexture);
        assertEquals("M3", composer.lastInterval.toString());
    }

    @Test
    void nullOverridesAndFormatFallBackToSavedSettingsAndWav() {
        AudioContent content = useCase.generateInterval(new IntervalSoundCommand("M3", "stacked", null, null));

        assertEquals(SoundSettings.defaults(), composer.lastSettings);
        assertEquals("interval-M3.wav", content.fileName());
        assertEquals("audio/wav", content.mimeType());
        assertEquals(InstrumentType.PIANO, synthesizer.lastInstrument);
    }

    @Test
    void acceptsAnyCaseForEveryParameter() {
        useCase.generateInterval(new IntervalSoundCommand("M3", "Descending",
                new SoundSettingsPatch(null, null, null, "Piano"), "Wav"));
        useCase.generateIntervalRange(new IntervalRangeSoundCommand("M3", "stacked", "Up", null, "WAV"));

        assertEquals(Direction.UP, composer.lastDirection);
        assertEquals(2, synthesizer.calls);
    }

    @RepeatedTest(25)
    void startPitchLeavesRoomForTheUpperNote() {
        useCase.generateInterval(new IntervalSoundCommand("P8", "ascending", null, null));

        int start = composer.lastStart.toMidiNumber();
        assertTrue(start >= 21 && start + 12 <= 108, "start " + start);
    }

    @Test
    void rejectsAnUnsupportedFormatWithoutSynthesizing() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.generateInterval(new IntervalSoundCommand("M3", "ascending", null, "mp3")));
        assertEquals(0, synthesizer.calls);
    }

    @Test
    void rejectsAnUnknownInstrumentName() {
        assertThrows(IllegalArgumentException.class, () -> useCase.generateInterval(new IntervalSoundCommand(
                "M3", "ascending", new SoundSettingsPatch(null, null, null, "violin"), null)));
    }

    @Test
    void rejectsAnInstrumentWithoutConfiguredRange() {
        var noInstruments = useCaseWith();

        assertThrows(IllegalArgumentException.class,
                () -> noInstruments.generateInterval(new IntervalSoundCommand("M3", "ascending", null, null)));
    }

    @Test
    void rejectsAnIntervalWiderThanTheInstrument() {
        var narrow = useCaseWith(FakeInstrumentRepository.piano("C4", "E4"));

        assertThrows(IllegalArgumentException.class,
                () -> narrow.generateInterval(new IntervalSoundCommand("P5", "ascending", null, null)));
        assertEquals(0, synthesizer.calls);
    }

    @Test
    void rejectsOutOfBoundsOverrides() {
        assertThrows(IllegalArgumentException.class, () -> useCase.generateInterval(new IntervalSoundCommand(
                "M3", "ascending", new SoundSettingsPatch(null, null, 0, null), null)));
    }

    @Test
    void generateIntervalRangePassesDirectionAndNamesTheFile() {
        AudioContent content = useCase.generateIntervalRange(
                new IntervalRangeSoundCommand("M3", "ascending", "down", null, null));

        assertEquals(Direction.DOWN, composer.lastDirection);
        assertEquals("interval-range-M3.wav", content.fileName());
    }

    @Test
    void generateIntervalRangeRejectsAnUnknownDirection() {
        assertThrows(IllegalArgumentException.class, () -> useCase.generateIntervalRange(
                new IntervalRangeSoundCommand("M3", "ascending", "sideways", null, null)));
    }
}
```

`src/test/java/vn/ktt/music/application/settings/SoundSettingsQueryUseCaseTest.java`:

```java
package vn.ktt.music.application.settings;

import org.junit.jupiter.api.Test;
import vn.ktt.music.application.settings.dto.SoundSettingsDTO;
import vn.ktt.music.application.sound.AudioEncoderRegistry;
import vn.ktt.music.application.sound.audio.AudioFormat;
import vn.ktt.music.application.support.FakeAudioEncoder;
import vn.ktt.music.application.support.FakeInstrumentRepository;
import vn.ktt.music.application.support.FakeSoundSettingsRepository;
import vn.ktt.music.domain.sound.valueobject.SoundSettings;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SoundSettingsQueryUseCaseTest {

    @Test
    void returnsSavedSettingsWithAvailableChoices() {
        var instruments = new FakeInstrumentRepository(FakeInstrumentRepository.piano());
        var assembler = new SoundSettingsDTOAssembler(instruments,
                new AudioEncoderRegistry(List.of(new FakeAudioEncoder(AudioFormat.WAV))));
        var useCase = new SoundSettingsQueryUseCase(new FakeSoundSettingsRepository(SoundSettings.defaults()), assembler);

        SoundSettingsDTO dto = useCase.getSettings();

        assertEquals(new SoundSettingsDTO(188, 21, 71, "PIANO", List.of("PIANO"), List.of("WAV")), dto);
    }
}
```

`src/test/java/vn/ktt/music/application/settings/UpdateSoundSettingsUseCaseTest.java`:

```java
package vn.ktt.music.application.settings;

import org.junit.jupiter.api.Test;
import vn.ktt.music.application.settings.dto.SoundSettingsDTO;
import vn.ktt.music.application.settings.dto.SoundSettingsPatch;
import vn.ktt.music.application.sound.AudioEncoderRegistry;
import vn.ktt.music.application.sound.audio.AudioFormat;
import vn.ktt.music.application.support.FakeAudioEncoder;
import vn.ktt.music.application.support.FakeInstrumentRepository;
import vn.ktt.music.application.support.FakeSoundSettingsRepository;
import vn.ktt.music.domain.instrument.Instrument;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.domain.sound.valueobject.SoundSettings;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateSoundSettingsUseCaseTest {

    private final FakeSoundSettingsRepository settingsRepository = new FakeSoundSettingsRepository(SoundSettings.defaults());

    private UpdateSoundSettingsUseCase useCaseWith(Instrument... instruments) {
        var instrumentRepository = new FakeInstrumentRepository(instruments);
        var assembler = new SoundSettingsDTOAssembler(instrumentRepository,
                new AudioEncoderRegistry(List.of(new FakeAudioEncoder(AudioFormat.WAV))));
        return new UpdateSoundSettingsUseCase(settingsRepository, instrumentRepository, assembler);
    }

    private final UpdateSoundSettingsUseCase useCase = useCaseWith(FakeInstrumentRepository.piano());

    @Test
    void partialPatchKeepsOtherFields() {
        SoundSettingsDTO dto = useCase.update(new SoundSettingsPatch(null, 0, null, null));

        assertEquals(new SoundSettings(188, 0, 71, InstrumentType.PIANO), settingsRepository.stored);
        assertEquals(0, dto.silenceMs());
        assertEquals(1, settingsRepository.saves);
    }

    @Test
    void explicitNullsChangeNothing() {
        useCase.update(new SoundSettingsPatch(null, null, null, null));

        assertEquals(SoundSettings.defaults(), settingsRepository.stored);
    }

    @Test
    void nullPatchIsTreatedAsEmpty() {
        useCase.update(null);

        assertEquals(SoundSettings.defaults(), settingsRepository.stored);
    }

    @Test
    void rejectsOutOfBoundsValuesAndSavesNothing() {
        assertThrows(IllegalArgumentException.class, () -> useCase.update(new SoundSettingsPatch(null, null, 0, null)));
        assertEquals(0, settingsRepository.saves);
    }

    @Test
    void rejectsAnUnknownInstrumentAndSavesNothing() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.update(new SoundSettingsPatch(null, null, null, "violin")));
        assertEquals(0, settingsRepository.saves);
    }

    @Test
    void rejectsAnInstrumentWithoutConfiguredRangeAndSavesNothing() {
        var noInstruments = useCaseWith();

        assertThrows(IllegalArgumentException.class, () -> noInstruments.update(SoundSettingsPatch.EMPTY));
        assertEquals(0, settingsRepository.saves);
    }
}
```

- [ ] **Step 3: Run the tests to verify they fail**

Run: `mvn test -Dtest='SoundSettingsPatchTest,IntervalSoundUseCaseTest,SoundSettingsQueryUseCaseTest,UpdateSoundSettingsUseCaseTest'`
Expected: BUILD FAILURE with `cannot find symbol` (`SoundSettingsPatch`, `IntervalSoundUseCase`, …).

- [ ] **Step 4: Implement the DTOs and ports**

`src/main/java/vn/ktt/music/application/settings/dto/SoundSettingsPatch.java`:

```java
package vn.ktt.music.application.settings.dto;

import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.domain.sound.valueobject.SoundSettings;

// Null (or absent) fields keep the current value; there is no reset-to-default.
public record SoundSettingsPatch(Integer noteDurationMs, Integer silenceMs, Integer loudness, String instrument) {

    public static final SoundSettingsPatch EMPTY = new SoundSettingsPatch(null, null, null, null);

    public SoundSettings applyTo(SoundSettings base) {
        return base.withOverrides(noteDurationMs, silenceMs, loudness,
                instrument == null ? null : InstrumentType.fromString(instrument));
    }
}
```

`src/main/java/vn/ktt/music/application/settings/dto/SoundSettingsDTO.java`:

```java
package vn.ktt.music.application.settings.dto;

import java.util.List;

public record SoundSettingsDTO(int noteDurationMs, int silenceMs, int loudness, String instrument,
                               List<String> availableInstruments, List<String> availableFormats) {
}
```

`src/main/java/vn/ktt/music/application/settings/inbound/ISoundSettingsQueryPort.java`:

```java
package vn.ktt.music.application.settings.inbound;

import vn.ktt.music.application.settings.dto.SoundSettingsDTO;

public interface ISoundSettingsQueryPort {
    SoundSettingsDTO getSettings();
}
```

`src/main/java/vn/ktt/music/application/settings/inbound/IUpdateSoundSettingsPort.java`:

```java
package vn.ktt.music.application.settings.inbound;

import vn.ktt.music.application.settings.dto.SoundSettingsDTO;
import vn.ktt.music.application.settings.dto.SoundSettingsPatch;

public interface IUpdateSoundSettingsPort {
    SoundSettingsDTO update(SoundSettingsPatch patch);
}
```

`src/main/java/vn/ktt/music/application/sound/dto/IntervalSoundCommand.java`:

```java
package vn.ktt.music.application.sound.dto;

import vn.ktt.music.application.settings.dto.SoundSettingsPatch;

public record IntervalSoundCommand(String interval, String texture, SoundSettingsPatch overrides, String format) {
}
```

`src/main/java/vn/ktt/music/application/sound/dto/IntervalRangeSoundCommand.java`:

```java
package vn.ktt.music.application.sound.dto;

import vn.ktt.music.application.settings.dto.SoundSettingsPatch;

public record IntervalRangeSoundCommand(String interval, String texture, String direction,
                                        SoundSettingsPatch overrides, String format) {
}
```

`src/main/java/vn/ktt/music/application/sound/inbound/IIntervalSoundPort.java`:

```java
package vn.ktt.music.application.sound.inbound;

import vn.ktt.music.application.sound.dto.AudioContent;
import vn.ktt.music.application.sound.dto.IntervalRangeSoundCommand;
import vn.ktt.music.application.sound.dto.IntervalSoundCommand;

public interface IIntervalSoundPort {
    AudioContent generateInterval(IntervalSoundCommand command);

    AudioContent generateIntervalRange(IntervalRangeSoundCommand command);
}
```

- [ ] **Step 5: Implement the use cases**

`src/main/java/vn/ktt/music/application/sound/IntervalSoundUseCase.java`:

```java
package vn.ktt.music.application.sound;

import org.springframework.stereotype.Service;
import vn.ktt.music.application.settings.dto.SoundSettingsPatch;
import vn.ktt.music.application.sound.audio.AudioFormat;
import vn.ktt.music.application.sound.dto.AudioContent;
import vn.ktt.music.application.sound.dto.IntervalRangeSoundCommand;
import vn.ktt.music.application.sound.dto.IntervalSoundCommand;
import vn.ktt.music.application.sound.inbound.IIntervalSoundPort;
import vn.ktt.music.domain.atom.Pitch;
import vn.ktt.music.domain.composition.Interval;
import vn.ktt.music.domain.factory.IMusicalEntityFactory;
import vn.ktt.music.domain.instrument.Instrument;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.domain.instrument.repository.IInstrumentRepository;
import vn.ktt.music.domain.service.IIntervalScoreComposer;
import vn.ktt.music.domain.service.IMusicalOperation;
import vn.ktt.music.domain.sound.repository.ISoundSettingsRepository;
import vn.ktt.music.domain.sound.valueobject.Direction;
import vn.ktt.music.domain.sound.valueobject.Score;
import vn.ktt.music.domain.sound.valueobject.SoundSettings;

@Service
public class IntervalSoundUseCase implements IIntervalSoundPort {

    private final IMusicalEntityFactory musicalEntityFactory;
    private final IMusicalOperation musicalOperation;
    private final IIntervalScoreComposer scoreComposer;
    private final ISoundSettingsRepository settingsRepository;
    private final IInstrumentRepository instrumentRepository;
    private final SoundRenderingService renderingService;

    public IntervalSoundUseCase(IMusicalEntityFactory musicalEntityFactory,
                                IMusicalOperation musicalOperation,
                                IIntervalScoreComposer scoreComposer,
                                ISoundSettingsRepository settingsRepository,
                                IInstrumentRepository instrumentRepository,
                                SoundRenderingService renderingService) {
        this.musicalEntityFactory = musicalEntityFactory;
        this.musicalOperation = musicalOperation;
        this.scoreComposer = scoreComposer;
        this.settingsRepository = settingsRepository;
        this.instrumentRepository = instrumentRepository;
        this.renderingService = renderingService;
    }

    @Override
    public AudioContent generateInterval(IntervalSoundCommand command) {
        Interval interval = musicalEntityFactory.getInterval(command.interval());
        Interval.Texture texture = Interval.Texture.fromString(command.texture());
        AudioFormat format = parseFormat(command.format());
        SoundSettings settings = resolveSettings(command.overrides());
        Instrument instrument = findInstrument(settings.instrument());

        // Compare MIDI numbers: Pitch.compareTo orders by spelling, so A#0 sorts below Bb0.
        int halfSteps = interval.getIntervalType().getHalfSteps();
        if (instrument.getHighestPitch().toMidiNumber() - halfSteps < instrument.getLowestPitch().toMidiNumber()) {
            throw new IllegalArgumentException("Interval " + interval + " does not fit instrument range");
        }
        Pitch upperBound = musicalOperation.getLowerBoundPitchFromInterval(
                instrument.getHighestPitch(), interval.getIntervalType());
        Pitch start = musicalOperation.getRandomPitch(instrument.getLowestPitch(), upperBound);

        Score score = scoreComposer.single(interval, texture, start, settings);
        return renderingService.render(score, settings.instrument(), format, "interval-" + interval);
    }

    @Override
    public AudioContent generateIntervalRange(IntervalRangeSoundCommand command) {
        Interval interval = musicalEntityFactory.getInterval(command.interval());
        Interval.Texture texture = Interval.Texture.fromString(command.texture());
        Direction direction = Direction.fromString(command.direction());
        AudioFormat format = parseFormat(command.format());
        SoundSettings settings = resolveSettings(command.overrides());
        Instrument instrument = findInstrument(settings.instrument());

        Score score = scoreComposer.range(interval, texture, direction, instrument, settings);
        return renderingService.render(score, settings.instrument(), format, "interval-range-" + interval);
    }

    private SoundSettings resolveSettings(SoundSettingsPatch overrides) {
        SoundSettingsPatch patch = overrides == null ? SoundSettingsPatch.EMPTY : overrides;
        return patch.applyTo(settingsRepository.load());
    }

    private Instrument findInstrument(InstrumentType type) {
        return instrumentRepository.findByType(type)
                .orElseThrow(() -> new IllegalArgumentException("Instrument is not configured: " + type));
    }

    private static AudioFormat parseFormat(String format) {
        return format == null ? AudioFormat.WAV : AudioFormat.fromString(format);
    }
}
```

`src/main/java/vn/ktt/music/application/settings/SoundSettingsDTOAssembler.java`:

```java
package vn.ktt.music.application.settings;

import org.springframework.stereotype.Component;
import vn.ktt.music.application.settings.dto.SoundSettingsDTO;
import vn.ktt.music.application.sound.AudioEncoderRegistry;
import vn.ktt.music.domain.instrument.repository.IInstrumentRepository;
import vn.ktt.music.domain.sound.valueobject.SoundSettings;

@Component
public class SoundSettingsDTOAssembler {
    private final IInstrumentRepository instrumentRepository;
    private final AudioEncoderRegistry encoderRegistry;

    public SoundSettingsDTOAssembler(IInstrumentRepository instrumentRepository, AudioEncoderRegistry encoderRegistry) {
        this.instrumentRepository = instrumentRepository;
        this.encoderRegistry = encoderRegistry;
    }

    public SoundSettingsDTO toDTO(SoundSettings settings) {
        return new SoundSettingsDTO(
                settings.noteDurationMs(),
                settings.silenceMs(),
                settings.loudness(),
                settings.instrument().name(),
                instrumentRepository.findAll().stream().map(instrument -> instrument.getInstrumentType().name()).toList(),
                encoderRegistry.supportedFormats().stream().map(Enum::name).toList());
    }
}
```

`src/main/java/vn/ktt/music/application/settings/SoundSettingsQueryUseCase.java`:

```java
package vn.ktt.music.application.settings;

import org.springframework.stereotype.Service;
import vn.ktt.music.application.settings.dto.SoundSettingsDTO;
import vn.ktt.music.application.settings.inbound.ISoundSettingsQueryPort;
import vn.ktt.music.domain.sound.repository.ISoundSettingsRepository;

@Service
public class SoundSettingsQueryUseCase implements ISoundSettingsQueryPort {
    private final ISoundSettingsRepository settingsRepository;
    private final SoundSettingsDTOAssembler assembler;

    public SoundSettingsQueryUseCase(ISoundSettingsRepository settingsRepository, SoundSettingsDTOAssembler assembler) {
        this.settingsRepository = settingsRepository;
        this.assembler = assembler;
    }

    @Override
    public SoundSettingsDTO getSettings() {
        return assembler.toDTO(settingsRepository.load());
    }
}
```

`src/main/java/vn/ktt/music/application/settings/UpdateSoundSettingsUseCase.java`:

```java
package vn.ktt.music.application.settings;

import org.springframework.stereotype.Service;
import vn.ktt.music.application.settings.dto.SoundSettingsDTO;
import vn.ktt.music.application.settings.dto.SoundSettingsPatch;
import vn.ktt.music.application.settings.inbound.IUpdateSoundSettingsPort;
import vn.ktt.music.domain.instrument.repository.IInstrumentRepository;
import vn.ktt.music.domain.sound.repository.ISoundSettingsRepository;
import vn.ktt.music.domain.sound.valueobject.SoundSettings;

@Service
public class UpdateSoundSettingsUseCase implements IUpdateSoundSettingsPort {
    private final ISoundSettingsRepository settingsRepository;
    private final IInstrumentRepository instrumentRepository;
    private final SoundSettingsDTOAssembler assembler;

    public UpdateSoundSettingsUseCase(ISoundSettingsRepository settingsRepository,
                                      IInstrumentRepository instrumentRepository,
                                      SoundSettingsDTOAssembler assembler) {
        this.settingsRepository = settingsRepository;
        this.instrumentRepository = instrumentRepository;
        this.assembler = assembler;
    }

    // Writes one table only, so the missing @Transactional is not an atomicity problem here.
    @Override
    public SoundSettingsDTO update(SoundSettingsPatch patch) {
        SoundSettingsPatch effectivePatch = patch == null ? SoundSettingsPatch.EMPTY : patch;
        SoundSettings updated = effectivePatch.applyTo(settingsRepository.load());
        if (instrumentRepository.findByType(updated.instrument()).isEmpty()) {
            throw new IllegalArgumentException("Instrument is not configured: " + updated.instrument());
        }
        settingsRepository.save(updated);
        return assembler.toDTO(updated);
    }
}
```

- [ ] **Step 6: Run the tests to verify they pass**

Run: `mvn test -Dtest='SoundSettingsPatchTest,IntervalSoundUseCaseTest,SoundSettingsQueryUseCaseTest,UpdateSoundSettingsUseCaseTest'`
Expected: BUILD SUCCESS. `IntervalSoundUseCaseTest` has 11 methods, and the repeated test runs 25 times.

Run: `mvn test`
Expected: BUILD SUCCESS.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/vn/ktt/music/application src/test/java/vn/ktt/music/application
git commit -F - <<'EOF'
feat(music): add interval sound and sound settings use cases

UC-1/UC-2 share IIntervalSoundPort. UC-3/UC-4 split into query and
update ports. Settings resolve as request override, then saved, then
built-in default.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
EOF
```

---

### Task 8: Controllers and the sound settings endpoint

**Files:**
- Modify: `pom.xml` (add the test dependency after `spring-boot-starter-test`, around lines 74–78)
- Create: `src/main/java/vn/ktt/music/infrastructure/controller/AudioResponses.java`
- Modify (replace): `src/main/java/vn/ktt/music/infrastructure/controller/IntervalsController.java`
- Modify (replace): `src/main/java/vn/ktt/music/infrastructure/controller/IntervalRangeController.java`
- Create: `src/main/java/vn/ktt/music/infrastructure/controller/SoundSettingsController.java`
- Test: `src/test/java/vn/ktt/music/infrastructure/controller/IntervalsControllerTest.java`
- Test: `src/test/java/vn/ktt/music/infrastructure/controller/IntervalRangeControllerTest.java`
- Test: `src/test/java/vn/ktt/music/infrastructure/controller/SoundSettingsControllerTest.java`

**Interfaces:**
- Consumes (from Task 7): `IIntervalSoundPort`, `ISoundSettingsQueryPort`, `IUpdateSoundSettingsPort`, `IntervalSoundCommand`, `IntervalRangeSoundCommand`, `SoundSettingsPatch`, `SoundSettingsDTO`. Also `AudioContent` from Task 3.
- Produces:
  - HTTP `GET /api/intervals/{interval}/random`, `GET /api/interval-range/{interval}`, `GET /api/sound-settings` and `PATCH /api/sound-settings`
  - A package-private `AudioResponses.attachment(AudioContent)`

- [ ] **Step 1: Add the test dependency**

In `pom.xml`, directly after the `spring-boot-starter-test` dependency block, add:

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc-test</artifactId>
            <scope>test</scope>
        </dependency>
```

Run: `mvn -q dependency:resolve`
Expected: exit code 0. The parent BOM manages the version, which is 4.1.1.

- [ ] **Step 2: Write the failing tests**

`src/test/java/vn/ktt/music/infrastructure/controller/IntervalsControllerTest.java`:

```java
package vn.ktt.music.infrastructure.controller;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.ktt.music.application.settings.dto.SoundSettingsPatch;
import vn.ktt.music.application.sound.dto.AudioContent;
import vn.ktt.music.application.sound.dto.IntervalSoundCommand;
import vn.ktt.music.application.sound.inbound.IIntervalSoundPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IntervalsController.class)
class IntervalsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private IIntervalSoundPort intervalSoundPort;

    @Test
    void bindsOverridesAndReturnsAudioFromTheContent() throws Exception {
        when(intervalSoundPort.generateInterval(any()))
                .thenReturn(new AudioContent(new byte[]{1, 2}, "audio/wav", "interval-M3.wav"));

        mvc.perform(get("/api/intervals/M3/random")
                        .param("texture", "ascending")
                        .param("noteDurationMs", "300")
                        .param("loudness", "50")
                        .param("instrument", "piano")
                        .param("format", "wav"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "audio/wav"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"interval-M3.wav\""))
                .andExpect(content().bytes(new byte[]{1, 2}));

        ArgumentCaptor<IntervalSoundCommand> command = ArgumentCaptor.forClass(IntervalSoundCommand.class);
        verify(intervalSoundPort).generateInterval(command.capture());
        assertEquals(new IntervalSoundCommand("M3", "ascending",
                new SoundSettingsPatch(300, null, 50, "piano"), "wav"), command.getValue());
    }

    @Test
    void legacyRequestSendsEmptyOverridesAndNoFormat() throws Exception {
        when(intervalSoundPort.generateInterval(any()))
                .thenReturn(new AudioContent(new byte[]{1}, "audio/wav", "interval-P5.wav"));

        mvc.perform(get("/api/intervals/P5/random").param("texture", "stacked"))
                .andExpect(status().isOk());

        ArgumentCaptor<IntervalSoundCommand> command = ArgumentCaptor.forClass(IntervalSoundCommand.class);
        verify(intervalSoundPort).generateInterval(command.capture());
        assertEquals(new IntervalSoundCommand("P5", "stacked", SoundSettingsPatch.EMPTY, null), command.getValue());
    }

    @Test
    void nonNumericOverrideIsABadRequest() throws Exception {
        mvc.perform(get("/api/intervals/M3/random").param("texture", "ascending").param("loudness", "loud"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(intervalSoundPort);
    }
}
```

`src/test/java/vn/ktt/music/infrastructure/controller/IntervalRangeControllerTest.java`:

```java
package vn.ktt.music.infrastructure.controller;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.ktt.music.application.settings.dto.SoundSettingsPatch;
import vn.ktt.music.application.sound.dto.AudioContent;
import vn.ktt.music.application.sound.dto.IntervalRangeSoundCommand;
import vn.ktt.music.application.sound.inbound.IIntervalSoundPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IntervalRangeController.class)
class IntervalRangeControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private IIntervalSoundPort intervalSoundPort;

    @Test
    void passesDirectionAndOverridesThrough() throws Exception {
        when(intervalSoundPort.generateIntervalRange(any()))
                .thenReturn(new AudioContent(new byte[]{1}, "audio/wav", "interval-range-M3.wav"));

        mvc.perform(get("/api/interval-range/M3")
                        .param("texture", "descending")
                        .param("direction", "down")
                        .param("silenceMs", "0"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "audio/wav"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"interval-range-M3.wav\""));

        ArgumentCaptor<IntervalRangeSoundCommand> command = ArgumentCaptor.forClass(IntervalRangeSoundCommand.class);
        verify(intervalSoundPort).generateIntervalRange(command.capture());
        assertEquals(new IntervalRangeSoundCommand("M3", "descending", "down",
                new SoundSettingsPatch(null, 0, null, null), null), command.getValue());
    }
}
```

`src/test/java/vn/ktt/music/infrastructure/controller/SoundSettingsControllerTest.java`:

```java
package vn.ktt.music.infrastructure.controller;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.ktt.music.application.settings.dto.SoundSettingsDTO;
import vn.ktt.music.application.settings.dto.SoundSettingsPatch;
import vn.ktt.music.application.settings.inbound.ISoundSettingsQueryPort;
import vn.ktt.music.application.settings.inbound.IUpdateSoundSettingsPort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SoundSettingsController.class)
class SoundSettingsControllerTest {

    private static final SoundSettingsDTO DTO =
            new SoundSettingsDTO(188, 21, 71, "PIANO", List.of("PIANO"), List.of("WAV"));

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ISoundSettingsQueryPort queryPort;

    @MockitoBean
    private IUpdateSoundSettingsPort updatePort;

    @Test
    void getReturnsTheSettings() throws Exception {
        when(queryPort.getSettings()).thenReturn(DTO);

        mvc.perform(get("/api/sound-settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noteDurationMs").value(188))
                .andExpect(jsonPath("$.instrument").value("PIANO"))
                .andExpect(jsonPath("$.availableFormats[0]").value("WAV"));
    }

    @Test
    void patchBindsPartialBodyWithExplicitNull() throws Exception {
        when(updatePort.update(any())).thenReturn(DTO);

        mvc.perform(patch("/api/sound-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loudness\": 50, \"instrument\": null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loudness").value(71));

        ArgumentCaptor<SoundSettingsPatch> patch = ArgumentCaptor.forClass(SoundSettingsPatch.class);
        verify(updatePort).update(patch.capture());
        assertEquals(new SoundSettingsPatch(null, null, 50, null), patch.getValue());
    }

    @Test
    void unknownFieldsAreIgnored() throws Exception {
        when(updatePort.update(any())).thenReturn(DTO);

        mvc.perform(patch("/api/sound-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loudnes\": 50}"))
                .andExpect(status().isOk());

        ArgumentCaptor<SoundSettingsPatch> patch = ArgumentCaptor.forClass(SoundSettingsPatch.class);
        verify(updatePort).update(patch.capture());
        assertEquals(SoundSettingsPatch.EMPTY, patch.getValue());
    }

    @Test
    void malformedJsonIsABadRequest() throws Exception {
        mvc.perform(patch("/api/sound-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loudness\":"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(updatePort);
    }
}
```

**About `unknownFieldsAreIgnored`:** it pins the behavior documented in spec §4 ("Unknown JSON fields are ignored"). If it fails with a 400, Spring Boot 4's Jackson 3 mapper rejects unknown properties. In that case:
1. Change the expectation to `status().isBadRequest()` and replace the captor block with `verifyNoInteractions(updatePort);`.
2. Change the spec §4 sentence to "Unknown JSON fields are rejected with 400."
3. Change the matching row in §7.
4. Mention the change in the task report.

Don't configure the mapper to change this behavior.

- [ ] **Step 3: Run the tests to verify they fail**

Run: `mvn test -Dtest='IntervalsControllerTest,IntervalRangeControllerTest,SoundSettingsControllerTest'`
Expected: BUILD FAILURE with `cannot find symbol: class SoundSettingsController`.

- [ ] **Step 4: Implement the controllers**

`src/main/java/vn/ktt/music/infrastructure/controller/AudioResponses.java`:

```java
package vn.ktt.music.infrastructure.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import vn.ktt.music.application.sound.dto.AudioContent;

final class AudioResponses {

    private AudioResponses() {
    }

    // @TODO: Not totally perfect, need refinement to obey the API standard contentType, header, etc.
    static ResponseEntity<byte[]> attachment(AudioContent audio) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(audio.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + audio.fileName() + "\"")
                .body(audio.data());
    }
}
```

Replace `src/main/java/vn/ktt/music/infrastructure/controller/IntervalsController.java` with:

```java
package vn.ktt.music.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.ktt.music.application.settings.dto.SoundSettingsPatch;
import vn.ktt.music.application.sound.dto.IntervalSoundCommand;
import vn.ktt.music.application.sound.inbound.IIntervalSoundPort;

@RestController
@RequestMapping("/api/intervals")
public class IntervalsController {
    private final IIntervalSoundPort intervalSoundPort;

    public IntervalsController(IIntervalSoundPort intervalSoundPort) {
        this.intervalSoundPort = intervalSoundPort;
    }

    @GetMapping("/{interval}/random")
    public ResponseEntity<byte[]> getRandomInterval(@PathVariable(name = "interval") String intervalNotation,
                                                    @RequestParam String texture,
                                                    @RequestParam(required = false) Integer noteDurationMs,
                                                    @RequestParam(required = false) Integer silenceMs,
                                                    @RequestParam(required = false) Integer loudness,
                                                    @RequestParam(required = false) String instrument,
                                                    @RequestParam(required = false) String format) {
        var overrides = new SoundSettingsPatch(noteDurationMs, silenceMs, loudness, instrument);
        return AudioResponses.attachment(intervalSoundPort.generateInterval(
                new IntervalSoundCommand(intervalNotation, texture, overrides, format)));
    }
}
```

Replace `src/main/java/vn/ktt/music/infrastructure/controller/IntervalRangeController.java` with:

```java
package vn.ktt.music.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.ktt.music.application.settings.dto.SoundSettingsPatch;
import vn.ktt.music.application.sound.dto.IntervalRangeSoundCommand;
import vn.ktt.music.application.sound.inbound.IIntervalSoundPort;

@RestController
@RequestMapping("/api/interval-range")
public class IntervalRangeController {

    private final IIntervalSoundPort intervalSoundPort;

    public IntervalRangeController(IIntervalSoundPort intervalSoundPort) {
        this.intervalSoundPort = intervalSoundPort;
    }

    @GetMapping("/{interval}")
    public ResponseEntity<byte[]> getIntervalRange(@PathVariable String interval,
                                                   @RequestParam String texture,
                                                   @RequestParam String direction,
                                                   @RequestParam(required = false) Integer noteDurationMs,
                                                   @RequestParam(required = false) Integer silenceMs,
                                                   @RequestParam(required = false) Integer loudness,
                                                   @RequestParam(required = false) String instrument,
                                                   @RequestParam(required = false) String format) {
        var overrides = new SoundSettingsPatch(noteDurationMs, silenceMs, loudness, instrument);
        return AudioResponses.attachment(intervalSoundPort.generateIntervalRange(
                new IntervalRangeSoundCommand(interval, texture, direction, overrides, format)));
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/controller/SoundSettingsController.java`:

```java
package vn.ktt.music.infrastructure.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.ktt.music.application.settings.dto.SoundSettingsDTO;
import vn.ktt.music.application.settings.dto.SoundSettingsPatch;
import vn.ktt.music.application.settings.inbound.ISoundSettingsQueryPort;
import vn.ktt.music.application.settings.inbound.IUpdateSoundSettingsPort;

@RestController
@RequestMapping("/api/sound-settings")
public class SoundSettingsController {
    private final ISoundSettingsQueryPort queryPort;
    private final IUpdateSoundSettingsPort updatePort;

    public SoundSettingsController(ISoundSettingsQueryPort queryPort, IUpdateSoundSettingsPort updatePort) {
        this.queryPort = queryPort;
        this.updatePort = updatePort;
    }

    @GetMapping
    public SoundSettingsDTO getSettings() {
        return queryPort.getSettings();
    }

    @PatchMapping
    public SoundSettingsDTO updateSettings(@RequestBody SoundSettingsPatch patch) {
        return updatePort.update(patch);
    }
}
```

- [ ] **Step 5: Run the tests to verify they pass**

Run: `mvn test -Dtest='IntervalsControllerTest,IntervalRangeControllerTest,SoundSettingsControllerTest'`
Expected: BUILD SUCCESS, with 8 tests passing. Apply the note under Step 2 only if `unknownFieldsAreIgnored` fails with a 400.

Run: `mvn test`
Expected: BUILD SUCCESS.

- [ ] **Step 6: Commit**

```bash
git add pom.xml src/main/java/vn/ktt/music/infrastructure/controller src/test/java/vn/ktt/music/infrastructure/controller
git commit -F - <<'EOF'
feat(music): expose sound overrides and /api/sound-settings

Interval endpoints accept optional noteDurationMs, silenceMs, loudness,
instrument and format parameters, and take Content-Type from the encoder.
Adds GET/PATCH /api/sound-settings.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
EOF
```

If the spec was edited under the note in Step 2, include `docs/superpowers/specs/2026-09-23-sound-generation-use-cases-design.md` in this commit.

---

### Task 9: Remove the old pipeline, move the factory bean, update docs, final verification

**Files:**
- Delete:
  - `src/main/java/vn/ktt/music/application/sound/IntervalGeneratorService.java`
  - `src/main/java/vn/ktt/music/application/sound/inbound/IIntervalGeneratorPort.java`
  - `src/main/java/vn/ktt/music/application/sound/outbound/ISoundGeneratorPort.java`
  - `src/main/java/vn/ktt/music/application/sound/dto/IntervalRangeParameters.java`
  - `src/main/java/vn/ktt/music/application/instrument/outbound/IInstrumentConfigurationPort.java`
  - `src/main/java/vn/ktt/music/infrastructure/adapter/MusicalConfigurationDataSourceAdapter.java`
  - `src/main/java/vn/ktt/music/infrastructure/audio/MidiSoundGenerator.java`
  - `src/main/java/vn/ktt/music/infrastructure/audio/midi/MidiSequenceBuilder.java`
- Modify: `src/main/java/vn/ktt/eartraining/infrastructure/config/DomainServiceConfig.java` (remove the imports at lines 13–14 and the `musicalEntityFactory` bean)
- Modify: `src/main/java/vn/ktt/music/infrastructure/config/MusicalDomainServiceConfig.java` (add the factory bean)
- Modify: `CLAUDE.md`

**Interfaces:**
- Consumes: all earlier tasks.
- Produces:
  - The `@Bean IMusicalEntityFactory musicalEntityFactory()` in `MusicalDomainServiceConfig`. The bean name is unchanged.
  - No `vn.ktt.music` imports anywhere under `vn.ktt.eartraining`.

- [ ] **Step 1: Confirm the old classes are unused**

Run:

```bash
grep -rn "IntervalGeneratorService\|IIntervalGeneratorPort\|ISoundGeneratorPort\|IntervalRangeParameters\|IInstrumentConfigurationPort\|MusicalConfigurationDataSourceAdapter\|MidiSoundGenerator\|MidiSequenceBuilder" src/ \
  | grep -v -e "/IntervalGeneratorService.java:" -e "/IIntervalGeneratorPort.java:" -e "/ISoundGeneratorPort.java:" \
            -e "/IntervalRangeParameters.java:" -e "/IInstrumentConfigurationPort.java:" \
            -e "/MusicalConfigurationDataSourceAdapter.java:" -e "/MidiSoundGenerator.java:" -e "/MidiSequenceBuilder.java:"
```

Expected: no output. If anything prints, stop and report it. Something outside the deleted set still uses the old pipeline.

- [ ] **Step 2: Delete the old pipeline**

```bash
git rm src/main/java/vn/ktt/music/application/sound/IntervalGeneratorService.java \
       src/main/java/vn/ktt/music/application/sound/inbound/IIntervalGeneratorPort.java \
       src/main/java/vn/ktt/music/application/sound/outbound/ISoundGeneratorPort.java \
       src/main/java/vn/ktt/music/application/sound/dto/IntervalRangeParameters.java \
       src/main/java/vn/ktt/music/application/instrument/outbound/IInstrumentConfigurationPort.java \
       src/main/java/vn/ktt/music/infrastructure/adapter/MusicalConfigurationDataSourceAdapter.java \
       src/main/java/vn/ktt/music/infrastructure/audio/MidiSoundGenerator.java \
       src/main/java/vn/ktt/music/infrastructure/audio/midi/MidiSequenceBuilder.java
```

- [ ] **Step 3: Move the `IMusicalEntityFactory` bean into the music context**

In `src/main/java/vn/ktt/eartraining/infrastructure/config/DomainServiceConfig.java`:
- Delete the two imports `vn.ktt.music.domain.factory.IMusicalEntityFactory` and `vn.ktt.music.domain.factory.MusicalEntityFactory`.
- Delete this method:

```java
    @Bean
    protected IMusicalEntityFactory musicalEntityFactory() {
        return new MusicalEntityFactory();
    }
```

Replace `src/main/java/vn/ktt/music/infrastructure/config/MusicalDomainServiceConfig.java` with:

```java
package vn.ktt.music.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.ktt.music.domain.factory.IMusicalEntityFactory;
import vn.ktt.music.domain.factory.MusicalEntityFactory;
import vn.ktt.music.domain.service.IIntervalScoreComposer;
import vn.ktt.music.domain.service.IMusicalOperation;
import vn.ktt.music.domain.service.IntervalScoreComposer;
import vn.ktt.music.domain.service.MusicalOperation;

@Configuration
public class MusicalDomainServiceConfig {

    @Bean
    public IMusicalOperation musicalOperation() {
        return new MusicalOperation();
    }

    @Bean
    public IIntervalScoreComposer intervalScoreComposer() {
        return new IntervalScoreComposer();
    }

    @Bean
    public IMusicalEntityFactory musicalEntityFactory() {
        return new MusicalEntityFactory();
    }
}
```

- [ ] **Step 4: Update CLAUDE.md**

In `CLAUDE.md`:

1. Replace the line:
   `mvn test                             # no DB needed (unit tests + one @JsonTest slice)`
   with:
   `mvn test                             # no DB needed (unit tests + @JsonTest/@WebMvcTest slices)`

2. Replace the `music` bullet:
   `` - `music`: pitches, intervals, and chords (`MusicalEntityFactory` parses notation like `M2`/`P5`), plus audio generation: MIDI `Sequence` → `IMidiRenderer` (SF2, or the `HarmonicMidiRenderer` oscillator) → PCM → `WavEncoder`. ``
   with:
   `` - `music`: pitches, intervals, and chords (`MusicalEntityFactory` parses notation like `M2`/`P5`), plus audio generation. The domain `IIntervalScoreComposer` builds a MIDI-free `Score`; `SoundRenderingService` runs `ISoundSynthesizerPort` (today `MidiSoundSynthesizer`: `ScoreToMidiSequenceConverter` at 1 tick = 1 ms → `IMidiRenderer`, SF2 or the `HarmonicMidiRenderer` oscillator) → `PcmAudio` → the `IAudioEncoderPort` that `AudioEncoderRegistry` picks by `AudioFormat` (only `WavAudioEncoder` today). Sound settings (note duration, silence, loudness, instrument) are saved in `musical_config` and can be overridden per request; resolution is request → saved → `SoundSettings.defaults()`. ``

3. Delete the bullet that starts with `- Known coupling leak:`.

4. Under `### Adding a type`, after the **New step context** list and before **Tests to update:**, add:

```markdown
**New audio format:**
- An `AudioFormat` constant.
- An `IAudioEncoderPort` `@Component` returning that constant from `format()`. `AudioEncoderRegistry` picks it up and fails at startup if two encoders claim the same format.
```

- [ ] **Step 5: Verify the architecture rules hold**

Run:

```bash
grep -rn "import vn.ktt.music" src/main/java/vn/ktt/eartraining
grep -rln "javax.sound" src/main/java/vn/ktt/music/domain src/main/java/vn/ktt/music/application
grep -rln "org.springframework" src/main/java/vn/ktt/music/domain
```

Expected: all three print nothing.

- [ ] **Step 6: Full build**

Run: `mvn clean package`
Expected: BUILD SUCCESS. All tests pass, including the six existing eartraining tests, and the gRPC stubs are generated.

- [ ] **Step 7: Manual smoke test**

Prerequisites: `docker compose up -d`, and the SF2 file present. If either is missing, skip this step and say so in the report.

Start the app with `mvn spring-boot:run`, then run:

```bash
curl -s -o /tmp/r1.wav -w "%{http_code} %{content_type}\n" "http://localhost:8080/api/intervals/M3/random?texture=ascending"
curl -s -o /tmp/r2.wav -w "%{http_code} %{content_type}\n" "http://localhost:8080/api/interval-range/P5?texture=stacked&direction=UP&noteDurationMs=300&loudness=90"
curl -s http://localhost:8080/api/sound-settings
curl -s -X PATCH -H 'Content-Type: application/json' -d '{"silenceMs": 100}' http://localhost:8080/api/sound-settings
curl -s -o /dev/null -w "%{http_code}\n" "http://localhost:8080/api/intervals/M3/random?texture=ascending&loudness=0"
```

Expected:
1. `200 audio/wav`
2. `200 audio/wav`
3. The JSON `{"noteDurationMs":188,"silenceMs":21,"loudness":71,"instrument":"PIANO","availableInstruments":["PIANO"],"availableFormats":["WAV"]}`
4. The same JSON with `"silenceMs":100`
5. `500`. This is expected until the planned `@RestControllerAdvice` maps `IllegalArgumentException` to 400.

Play `/tmp/r1.wav` if a player is available. It should be two piano notes with no click at the start.

Stop the app afterwards.

- [ ] **Step 8: Commit**

```bash
git add -A src/main/java CLAUDE.md
git commit -F - <<'EOF'
refactor(music): remove MIDI-bound sound generator and fix bean leak

Deletes IntervalGeneratorService and the MIDI/WAV-specific ports now
replaced by the score/synthesizer/encoder pipeline. Moves the
IMusicalEntityFactory bean into MusicalDomainServiceConfig, so
eartraining no longer imports the music context.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
EOF
```

# SF2 Sampler Synthesizer, Phase 1: Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace Java's built-in Gervill synthesizer and the whole `javax.sound.midi` pipeline with our own pure-Java SoundFont 2 sampler. It takes notes plus sound options and returns PCM. The interval endpoints use it with built-in defaults and return stereo WAV.

**Architecture:**
- **`vn.ktt.music.infrastructure.synth`:** a self-contained package with no Spring, `javax.sound`, Lombok or other `vn.ktt` imports.
  - It parses the `.sf2` file and memory-maps its samples.
  - It resolves SF2 zones into voices: interpolated sample read, resonant low-pass filter, DAHDSR volume envelope, constant-power pan.
  - It mixes the voices and returns float PCM.
- **Application layer:** a narrow port, `ISoundSynthesizerPort` (notes + instrument → `PcmAudio`). `IntervalNoteSequencer` lays out the notes, and `IAudioEncoderPort` turns PCM into WAV.
- **`SamplerSynthesizerAdapter`:** maps the port to the synth API and supplies the default options.

**Tech Stack:** Java 25, Spring Boot 4.1.1, `java.nio` (`FileChannel.map`), JUnit 5. No new dependencies.

**Spec:** `docs/superpowers/specs/2026-10-10-sf2-sampler-synthesizer-design.md`. Read it before starting any task. Section numbers below (§N) refer to it. This plan covers **Phase 1 only** (spec §7). Phases 2 and 3 get their own plans once this one lands.

**Verified:** every code block in this plan was compiled and run in a scratch copy of the repo on 2026-10-10. `mvn test` passed with 172 tests with the real SoundFont present, and with the smoke test skipped when it was absent.

## Global Constraints

**Build**
- JDK 25 and a system `mvn`; there is no Maven wrapper.
- `mvn test` must pass after every task, with no database and no SF2 file.
- `mvn spring-boot:run` needs PostgreSQL (`docker compose up -d`) and `src/main/resources/soundfonts/grand_piano.sf2`. From Task 9 on, the SoundFont is loaded at startup, and the app refuses to start without it.

**Architecture rules**
- Nothing under `vn.ktt.music.infrastructure.synth` imports `org.springframework`, `javax.sound`, `lombok`, or a `vn.ktt` package outside `vn.ktt.music.infrastructure.synth`. `SynthPackageIsolationTest` (Task 1) enforces this.
- Invalid input throws a plain `IllegalArgumentException`. A malformed SoundFont throws `SoundFontFormatException`. A missing SoundFont resource throws `IllegalStateException` at startup.
- Don't add `@ControllerAdvice`, `@Transactional`, or per-controller `try`/`catch` (CLAUDE.md).
- The sample data `ShortBuffer` is shared between concurrent renders. Only ever read it with absolute `get(int)`.

**Values fixed by the spec**
- Default options: preset `(0, 0)`, 44100 Hz, `STEREO`, 0 dB master gain, 1000 ms tail, `LINEAR` interpolation (§3.2, §5).
- Velocity attenuation: `960 × concave(1 − v/127)` cB, where `concave(x) = clamp(−(5/12)·log10(1 − x), 0, 1)`. The default velocity → filter modulator is **not** applied (§3.4).
- Envelope: attack is linear in amplitude; decay and release are linear in cB at 1000 cB per stage time; a voice is finished at 960 cB (§3.5).
- Filter: RBJ biquad low-pass, `Q = 10^((Qcb/10 − 3.01)/20)`, bypassed at ≥ 13500 cents or a cutoff ≥ 0.45 × the rate (§3.5).
- Pan: `θ = (pan/500 + 1)·π/4`, `L = cos θ`, `R = sin θ` (§3.5).
- Buffer: `max(onset + duration) + tailMs`, then master gain, a soft limiter at 0.8 that never exceeds 1, and a 5 ms fade-out. `MONO` is `(L + R)/2` (§3.6).
- One request may render at most `Mixer.MAX_DURATION_MS = 600000` ms. Longer requests throw `IllegalArgumentException`.
- Sequencer: `PREROLL_MS = 63`, `NOTE_MS = 188`, `STACKED_NOTE_MS = 376`, `GAP_MS = 21`, `VELOCITY = 90` (§4).
- File names stay `interval-<notation>.<ext>` and `interval-range-<notation>.<ext>`. The MIME type and extension come from `EncodedAudio`.

**Git**
- Work on branch `refactor/5-sound-generation`. Commit at the end of every task with the message given.

## Review Focus

These are the conditions the spec implies that are most likely to bite someone using this software. Each one is pinned by the test named in its owning task.

1. **Several requests rendering at the same time** produce correct, identical audio. All renders share one memory-mapped sample buffer, so a relative `get()` anywhere would corrupt other requests. Pinned in Task 7 (`concurrentRendersOfTheSameRequestAreIdentical`).
2. **Running from a packaged jar**, where the classpath SoundFont is not a plain file, still loads the SoundFont, by copying it to a temp file once. Pinned in Task 9 (`copiesASoundFontThatIsNotAPlainFile`).
3. **A unison (`P0`) interval, especially `STACKED`**, renders two identical notes without error, and a `P0` range has exactly one base. Pinned in Task 8 (`unisonStackedGivesTwoIdenticalNotes`, `unisonRangeHasASingleBase`).
4. **An absurd onset or duration** (for example, a note at 10 minutes) is rejected with `IllegalArgumentException`, instead of allocating gigabytes of buffer. Pinned in Task 7 (`requestsLongerThanTheMaximumAreRejected`).
5. **A corrupt or hand-edited SoundFont**, where a zone points at an instrument or sample that doesn't exist, fails at startup with a `SoundFontFormatException` that names the problem, not an `IndexOutOfBoundsException`. Pinned in Task 2 (`rejectsAZoneThatReferencesAMissingInstrument`, `rejectsSampleBoundsOutsideSampleChunk`).

## File map

```
src/main/java/vn/ktt/music/
  infrastructure/synth/                     (new; no Spring, javax.sound, Lombok)
    Synthesizer.java                        public facade: render(SynthRequest) -> SynthOutput
    api/      NoteSpec, PresetRef, ChannelLayout, OutputFormat, Interpolation,
              SynthOptions, SynthRequest, SynthOutput
    soundfont/ GeneratorType, Zone, SampleHeader, InstrumentZone, Instrument, PresetZone,
              Preset, SoundFont, SoundFontLoader, SoundFontFormatException
    engine/   Units, EnvelopeTimecents, VolumeEnvelope, LowPassFilter, SampleSource,
              Interpolator, VoiceSpec, ZoneResolver, Voice, Mixer
  application/sound/
    IntervalNoteSequencer.java              (new)
    IntervalGeneratorService.java           (rewritten in Task 10)
    audio/    SoundNote, PcmAudio, EncodedAudio       (new)
    outbound/ ISoundSynthesizerPort, IAudioEncoderPort (new; ISoundGeneratorPort deleted)
    dto/AudioContent.java                   (Lombok class -> record)
  infrastructure/
    audio/SamplerSynthesizerAdapter.java    (new)
    audio/encoder/WavAudioEncoder.java      (new; WavEncoder deleted)
    config/SynthesizerConfig.java           (new)
    controller/IntervalsController.java, IntervalRangeController.java (use AudioContent record)
    audio/MidiSoundGenerator, audio/midi/*, audio/renderer/*          (deleted in Task 10)
src/test/java/vn/ktt/music/...              one test class per unit, plus TestSoundFontBuilder
```

---

### Task 1: Synth API records and the isolation guard

**Files:**
- Create: `src/main/java/vn/ktt/music/infrastructure/synth/api/NoteSpec.java`, `PresetRef.java`, `ChannelLayout.java`, `OutputFormat.java`, `Interpolation.java`, `SynthOptions.java`, `SynthRequest.java`, `SynthOutput.java`
- Test: `src/test/java/vn/ktt/music/infrastructure/synth/api/SynthApiTest.java`, `src/test/java/vn/ktt/music/infrastructure/synth/SynthPackageIsolationTest.java`

**Interfaces:**
- Consumes: nothing.
- Produces (package `vn.ktt.music.infrastructure.synth.api`, all public):
  - `record NoteSpec(int midiKey, long onsetMs, long durationMs, int velocity)` with `long releaseMs()`
  - `record PresetRef(int bank, int program)`
  - `enum ChannelLayout { MONO, STEREO }` with `int count()`
  - `record OutputFormat(int sampleRate, ChannelLayout channels, double masterGainDb, long tailMs)` with `static final OutputFormat DEFAULT`
  - `enum Interpolation { LINEAR }`
  - `record SynthOptions(PresetRef preset, OutputFormat output, Interpolation interpolation)`, with `static SynthOptions defaults()`, `withPreset(PresetRef)`, `withOutput(OutputFormat)`, `withInterpolation(Interpolation)`, and `static final PresetRef DEFAULT_PRESET`
  - `record SynthRequest(List<NoteSpec> notes, SynthOptions options)`. `null` options become `SynthOptions.defaults()`.
  - `record SynthOutput(float[] samples, int sampleRate, int channels)` with `int frames()`

- [ ] **Step 1: Write the failing tests**

`src/test/java/vn/ktt/music/infrastructure/synth/api/SynthApiTest.java`:

```java
package vn.ktt.music.infrastructure.synth.api;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SynthApiTest {

    @Test
    void noteSpecAcceptsBoundaryValues() {
        assertDoesNotThrow(() -> new NoteSpec(0, 0, 1, 1));
        assertDoesNotThrow(() -> new NoteSpec(127, 10, 5000, 127));
    }

    @Test
    void noteSpecRejectsOutOfRangeValues() {
        assertThrows(IllegalArgumentException.class, () -> new NoteSpec(-1, 0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new NoteSpec(128, 0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new NoteSpec(60, -1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new NoteSpec(60, 0, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new NoteSpec(60, 0, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new NoteSpec(60, 0, 1, 128));
    }

    @Test
    void noteSpecReleaseIsOnsetPlusDuration() {
        assertEquals(272, new NoteSpec(60, 84, 188, 90).releaseMs());
    }

    @Test
    void presetRefBounds() {
        assertDoesNotThrow(() -> new PresetRef(16383, 127));
        assertThrows(IllegalArgumentException.class, () -> new PresetRef(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new PresetRef(16384, 0));
        assertThrows(IllegalArgumentException.class, () -> new PresetRef(0, 128));
    }

    @Test
    void outputFormatBounds() {
        assertDoesNotThrow(() -> new OutputFormat(8000, ChannelLayout.MONO, -60, 0));
        assertDoesNotThrow(() -> new OutputFormat(96000, ChannelLayout.STEREO, 12, 10_000));
        assertThrows(IllegalArgumentException.class, () -> new OutputFormat(7999, ChannelLayout.MONO, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new OutputFormat(96001, ChannelLayout.MONO, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new OutputFormat(44100, null, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new OutputFormat(44100, ChannelLayout.MONO, 12.1, 0));
        assertThrows(IllegalArgumentException.class, () -> new OutputFormat(44100, ChannelLayout.MONO, Double.NaN, 0));
        assertThrows(IllegalArgumentException.class, () -> new OutputFormat(44100, ChannelLayout.MONO, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> new OutputFormat(44100, ChannelLayout.MONO, 0, 10_001));
    }

    @Test
    void optionDefaults() {
        SynthOptions options = SynthOptions.defaults();
        assertEquals(new PresetRef(0, 0), options.preset());
        assertEquals(new OutputFormat(44100, ChannelLayout.STEREO, 0.0, 1000), options.output());
        assertEquals(Interpolation.LINEAR, options.interpolation());
    }

    @Test
    void withMethodsReplaceOneGroup() {
        OutputFormat mono = new OutputFormat(22050, ChannelLayout.MONO, -6, 500);
        SynthOptions options = SynthOptions.defaults().withPreset(new PresetRef(1, 2)).withOutput(mono);
        assertEquals(new PresetRef(1, 2), options.preset());
        assertEquals(mono, options.output());
        assertEquals(Interpolation.LINEAR, options.interpolation());
    }

    @Test
    void requestRejectsEmptyOrNullNotes() {
        assertThrows(IllegalArgumentException.class, () -> new SynthRequest(List.of(), null));
        assertThrows(IllegalArgumentException.class, () -> new SynthRequest(null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new SynthRequest(Arrays.asList(new NoteSpec(60, 0, 1, 1), null), null));
    }

    @Test
    void requestCopiesNotesAndDefaultsOptions() {
        List<NoteSpec> notes = new ArrayList<>(List.of(new NoteSpec(60, 0, 100, 90)));
        SynthRequest request = new SynthRequest(notes, null);
        notes.clear();
        assertEquals(1, request.notes().size());
        assertEquals(SynthOptions.defaults(), request.options());
    }

    @Test
    void outputValidatesShape() {
        assertEquals(2, new SynthOutput(new float[4], 44100, 2).frames());
        assertThrows(IllegalArgumentException.class, () -> new SynthOutput(new float[3], 44100, 2));
        assertThrows(IllegalArgumentException.class, () -> new SynthOutput(new float[2], 44100, 3));
        assertThrows(IllegalArgumentException.class, () -> new SynthOutput(null, 44100, 1));
        assertThrows(IllegalArgumentException.class, () -> new SynthOutput(new float[2], 0, 1));
    }
}
```

`src/test/java/vn/ktt/music/infrastructure/synth/SynthPackageIsolationTest.java`. It scans the synth sources for forbidden imports, and it must keep passing in every later task:

```java
package vn.ktt.music.infrastructure.synth;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** The synth package must stay liftable into its own service: no Spring, javax.sound, Lombok or other vn.ktt code. */
class SynthPackageIsolationTest {

    private static final Path SYNTH_ROOT = Path.of("src/main/java/vn/ktt/music/infrastructure/synth");
    private static final String SYNTH_PACKAGE = "vn.ktt.music.infrastructure.synth";

    @Test
    void synthPackageHasNoForbiddenImports() throws IOException {
        List<Path> sources;
        try (Stream<Path> files = Files.walk(SYNTH_ROOT)) {
            sources = files.filter(path -> path.toString().endsWith(".java")).toList();
        }
        assertFalse(sources.isEmpty(), "no sources found under " + SYNTH_ROOT.toAbsolutePath());

        List<String> violations = sources.stream()
                .flatMap(path -> readLines(path).stream()
                        .filter(SynthPackageIsolationTest::isForbiddenImport)
                        .map(line -> path + ": " + line.trim()))
                .toList();

        assertEquals(List.of(), violations);
    }

    private static boolean isForbiddenImport(String line) {
        String trimmed = line.trim();
        if (!trimmed.startsWith("import ")) {
            return false;
        }
        return trimmed.contains("org.springframework")
                || trimmed.contains("javax.sound")
                || trimmed.contains("lombok")
                || (trimmed.contains("vn.ktt.") && !trimmed.contains(SYNTH_PACKAGE));
    }

    private static List<String> readLines(Path path) {
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn test -Dtest='SynthApiTest,SynthPackageIsolationTest'`
Expected: `COMPILATION ERROR`, with `cannot find symbol` for `NoteSpec`, `SynthOptions` and the other records.

- [ ] **Step 3: Write the records**

`src/main/java/vn/ktt/music/infrastructure/synth/api/NoteSpec.java`:

```java
package vn.ktt.music.infrastructure.synth.api;

public record NoteSpec(int midiKey, long onsetMs, long durationMs, int velocity) {

    public NoteSpec {
        if (midiKey < 0 || midiKey > 127) {
            throw new IllegalArgumentException("midiKey must be 0-127: " + midiKey);
        }
        if (onsetMs < 0) {
            throw new IllegalArgumentException("onsetMs must be >= 0: " + onsetMs);
        }
        if (durationMs <= 0) {
            throw new IllegalArgumentException("durationMs must be > 0: " + durationMs);
        }
        if (velocity < 1 || velocity > 127) {
            throw new IllegalArgumentException("velocity must be 1-127: " + velocity);
        }
    }

    public long releaseMs() {
        return onsetMs + durationMs;
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/api/PresetRef.java`:

```java
package vn.ktt.music.infrastructure.synth.api;

public record PresetRef(int bank, int program) {

    public PresetRef {
        if (bank < 0 || bank > 16383) {
            throw new IllegalArgumentException("bank must be 0-16383: " + bank);
        }
        if (program < 0 || program > 127) {
            throw new IllegalArgumentException("program must be 0-127: " + program);
        }
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/api/ChannelLayout.java`:

```java
package vn.ktt.music.infrastructure.synth.api;

public enum ChannelLayout {
    MONO(1),
    STEREO(2);

    private final int count;

    ChannelLayout(int count) {
        this.count = count;
    }

    public int count() {
        return count;
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/api/OutputFormat.java`:

```java
package vn.ktt.music.infrastructure.synth.api;

public record OutputFormat(int sampleRate, ChannelLayout channels, double masterGainDb, long tailMs) {

    public static final OutputFormat DEFAULT = new OutputFormat(44100, ChannelLayout.STEREO, 0.0, 1000);

    public OutputFormat {
        if (sampleRate < 8000 || sampleRate > 96000) {
            throw new IllegalArgumentException("sampleRate must be 8000-96000: " + sampleRate);
        }
        if (channels == null) {
            throw new IllegalArgumentException("channels must not be null");
        }
        if (!(masterGainDb >= -60.0 && masterGainDb <= 12.0)) {
            throw new IllegalArgumentException("masterGainDb must be -60 to 12: " + masterGainDb);
        }
        if (tailMs < 0 || tailMs > 10_000) {
            throw new IllegalArgumentException("tailMs must be 0-10000: " + tailMs);
        }
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/api/Interpolation.java` (Phase 2 adds `NEAREST`, `CUBIC` and `SINC`):

```java
package vn.ktt.music.infrastructure.synth.api;

public enum Interpolation {
    LINEAR
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/api/SynthOptions.java`:

```java
package vn.ktt.music.infrastructure.synth.api;

/**
 * Sound options for one render. Groups with an engine default are never null after construction;
 * build instances with {@link #defaults()} and the {@code with...} methods so that new groups can be
 * added without breaking callers.
 */
public record SynthOptions(PresetRef preset, OutputFormat output, Interpolation interpolation) {

    public static final PresetRef DEFAULT_PRESET = new PresetRef(0, 0);

    public SynthOptions {
        preset = preset == null ? DEFAULT_PRESET : preset;
        output = output == null ? OutputFormat.DEFAULT : output;
        interpolation = interpolation == null ? Interpolation.LINEAR : interpolation;
    }

    public static SynthOptions defaults() {
        return new SynthOptions(null, null, null);
    }

    public SynthOptions withPreset(PresetRef newPreset) {
        return new SynthOptions(newPreset, output, interpolation);
    }

    public SynthOptions withOutput(OutputFormat newOutput) {
        return new SynthOptions(preset, newOutput, interpolation);
    }

    public SynthOptions withInterpolation(Interpolation newInterpolation) {
        return new SynthOptions(preset, output, newInterpolation);
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/api/SynthRequest.java`. Note the null check: `List.of(...).contains(null)` throws `NullPointerException`, so it uses a stream:

```java
package vn.ktt.music.infrastructure.synth.api;

import java.util.List;
import java.util.Objects;

public record SynthRequest(List<NoteSpec> notes, SynthOptions options) {

    public SynthRequest {
        if (notes == null || notes.isEmpty()) {
            throw new IllegalArgumentException("notes must not be empty");
        }
        if (notes.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("notes must not contain null");
        }
        notes = List.copyOf(notes);
        options = options == null ? SynthOptions.defaults() : options;
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/api/SynthOutput.java`:

```java
package vn.ktt.music.infrastructure.synth.api;

/** Interleaved float PCM in -1..1. The array is not copied. */
public record SynthOutput(float[] samples, int sampleRate, int channels) {

    public SynthOutput {
        if (samples == null) {
            throw new IllegalArgumentException("samples must not be null");
        }
        if (channels < 1 || channels > 2) {
            throw new IllegalArgumentException("channels must be 1 or 2: " + channels);
        }
        if (samples.length % channels != 0) {
            throw new IllegalArgumentException("samples length must be a multiple of channels");
        }
        if (sampleRate <= 0) {
            throw new IllegalArgumentException("sampleRate must be > 0: " + sampleRate);
        }
    }

    public int frames() {
        return samples.length / channels;
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `mvn test -Dtest='SynthApiTest,SynthPackageIsolationTest'`
Expected: `Tests run: 11, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/vn/ktt/music/infrastructure/synth/api src/test/java/vn/ktt/music/infrastructure/synth/api src/test/java/vn/ktt/music/infrastructure/synth/SynthPackageIsolationTest.java
git commit -F - <<'MSG'
feat(synth): add the synthesizer request and output API (#5)

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
MSG
```

---

### Task 2: SoundFont model and loader

**Files:**
- Create: `src/main/java/vn/ktt/music/infrastructure/synth/soundfont/SoundFontFormatException.java`, `GeneratorType.java`, `Zone.java`, `SampleHeader.java`, `InstrumentZone.java`, `Instrument.java`, `PresetZone.java`, `Preset.java`, `SoundFont.java`, `SoundFontLoader.java`
- Test: `src/test/java/vn/ktt/music/infrastructure/synth/TestSoundFontBuilder.java` (a shared test fixture, used again in Tasks 7 and 9), `src/test/java/vn/ktt/music/infrastructure/synth/soundfont/SoundFontLoaderTest.java`

**Interfaces:**
- Consumes: nothing from earlier tasks.
- Produces (package `vn.ktt.music.infrastructure.synth.soundfont`, all public):
  - `class SoundFontFormatException extends RuntimeException`
  - `enum GeneratorType`: constants named after the SF2 generators (`PAN`, `KEY_RANGE`, `SAMPLE_ID`, `INITIAL_FILTER_FC`, …). It has `static GeneratorType fromId(int)` (null for ignored ids), `int id()`, `int defaultValue()`, `boolean presetAllowed()` and `int clamp(int)`.
  - `final class Zone(Map<GeneratorType,Integer>)`, with `static final Zone EMPTY`, `boolean has(GeneratorType)`, `int get(GeneratorType)` (which returns the default when unset), and `static int rangeLow(int)`, `rangeHigh(int)` and `range(int low, int high)`
  - `record SampleHeader(String name, int start, int end, int loopStart, int loopEnd, int sampleRate, int originalPitch, int pitchCorrection, int sampleType)` with `boolean isRom()`
  - `record InstrumentZone(Zone zone, SampleHeader sample)` and `record Instrument(String name, Zone globalZone, List<InstrumentZone> zones)`
  - `record PresetZone(Zone zone, Instrument instrument)` and `record Preset(String name, int bank, int program, Zone globalZone, List<PresetZone> zones)`. A `null` global zone becomes `Zone.EMPTY`.
  - `final class SoundFont(List<Preset>, ShortBuffer)`, with `Optional<Preset> findPreset(int bank, int program)`, `List<Preset> presets()` and `ShortBuffer sampleData()`
  - `final class SoundFontLoader` with `static SoundFont load(Path)`
- Test fixture (package `vn.ktt.music.infrastructure.synth`, test sources): `TestSoundFontBuilder`, with:
  - `static gen(GeneratorType, int)`, `zone(Gen...)`, `range(int, int)`, `constant(int, int)`, `ramp(int, int)` and `sine(...)`
  - `static TestSoundFontBuilder singleZone(short[] data, Gen... instrumentZoneGens)`
  - `int addSample(...)`, `int addInstrument(...)` and `void addPreset(...)`
  - The corruption hooks `ifilMajor`, `formType`, `omitChunk`, `padChunk` and `overrideSampleBounds`
  - `byte[] build()` and `Path writeTo(Path dir)`

- [ ] **Step 1: Write the test fixture and the failing tests**

`src/test/java/vn/ktt/music/infrastructure/synth/TestSoundFontBuilder.java`. It writes a minimal but valid SF2: an `INFO` list (`ifil`, `INAM`), an `sdta` list (`smpl`, with 46 zero samples after each sample) and a `pdta` list with all nine tables and their terminal records:

```java
package vn.ktt.music.infrastructure.synth;

import vn.ktt.music.infrastructure.synth.soundfont.GeneratorType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Builds small, valid SF2 files for tests. Each zone is a list of generators; see SF2 2.04 sections 5-7. */
public final class TestSoundFontBuilder {

    public record Gen(GeneratorType type, int value) {
    }

    private record Sample(String name, short[] data, int sampleRate, int originalPitch, int sampleType,
                          int loopStart, int loopEnd) {
    }

    private record Named(String name, int bank, int program, List<List<Gen>> zones) {
    }

    private static final int SAMPLE_PADDING = 46;

    private final List<Sample> samples = new ArrayList<>();
    private final List<Named> instruments = new ArrayList<>();
    private final List<Named> presets = new ArrayList<>();
    private final Set<String> omittedChunks = new HashSet<>();
    private final Set<String> paddedChunks = new HashSet<>();
    private final Map<Integer, int[]> sampleBoundOverrides = new HashMap<>();
    private int ifilMajor = 2;
    private String formType = "sfbk";

    public static Gen gen(GeneratorType type, int value) {
        return new Gen(type, value);
    }

    public static List<Gen> zone(Gen... gens) {
        return List.of(gens);
    }

    public static int range(int low, int high) {
        return (low & 0xFF) | ((high & 0xFF) << 8);
    }

    public static short[] constant(int length, int value) {
        short[] data = new short[length];
        Arrays.fill(data, (short) value);
        return data;
    }

    public static short[] ramp(int length, int stepPerSample) {
        short[] data = new short[length];
        for (int i = 0; i < length; i++) {
            data[i] = (short) (i * stepPerSample);
        }
        return data;
    }

    public static short[] sine(int length, double frequencyHz, int sampleRate, int amplitude) {
        short[] data = new short[length];
        for (int i = 0; i < length; i++) {
            data[i] = (short) Math.round(amplitude * Math.sin(2 * Math.PI * frequencyHz * i / sampleRate));
        }
        return data;
    }

    /** One looped mono sample at 44.1 kHz, root key 60, in one instrument zone of preset bank 0 program 0. */
    public static TestSoundFontBuilder singleZone(short[] data, Gen... instrumentZoneGens) {
        TestSoundFontBuilder builder = new TestSoundFontBuilder();
        int sample = builder.addSample("sample", data, 44100, 60, 1, 0, data.length);
        List<Gen> zone = new ArrayList<>(List.of(instrumentZoneGens));
        zone.add(gen(GeneratorType.SAMPLE_ID, sample));
        int instrument = builder.addInstrument("instrument", List.of(zone));
        builder.addPreset("preset", 0, 0, List.of(zone(gen(GeneratorType.INSTRUMENT, instrument))));
        return builder;
    }

    /** Adds a sample; loop points are relative to the sample's first frame. Returns its sample id. */
    public int addSample(String name, short[] data, int sampleRate, int originalPitch, int sampleType,
                         int loopStart, int loopEnd) {
        samples.add(new Sample(name, data, sampleRate, originalPitch, sampleType, loopStart, loopEnd));
        return samples.size() - 1;
    }

    /** Adds an instrument; a first zone without SAMPLE_ID is its global zone. Returns its instrument id. */
    public int addInstrument(String name, List<List<Gen>> zones) {
        instruments.add(new Named(name, 0, 0, zones));
        return instruments.size() - 1;
    }

    /** Adds a preset; a first zone without INSTRUMENT is its global zone. */
    public void addPreset(String name, int bank, int program, List<List<Gen>> zones) {
        presets.add(new Named(name, bank, program, zones));
    }

    public TestSoundFontBuilder ifilMajor(int major) {
        this.ifilMajor = major;
        return this;
    }

    public TestSoundFontBuilder formType(String type) {
        this.formType = type;
        return this;
    }

    public TestSoundFontBuilder omitChunk(String id) {
        omittedChunks.add(id);
        return this;
    }

    /** Appends one stray byte to a chunk so its size is no longer a whole number of records. */
    public TestSoundFontBuilder padChunk(String id) {
        paddedChunks.add(id);
        return this;
    }

    public TestSoundFontBuilder overrideSampleBounds(int sampleId, int start, int end) {
        sampleBoundOverrides.put(sampleId, new int[]{start, end});
        return this;
    }

    public Path writeTo(Path directory) {
        try {
            Path file = Files.createTempFile(directory, "test-", ".sf2");
            Files.write(file, build());
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public byte[] build() {
        ByteArrayOutputStream smpl = new ByteArrayOutputStream();
        ByteArrayOutputStream shdr = new ByteArrayOutputStream();
        int offset = 0;
        for (int i = 0; i < samples.size(); i++) {
            Sample sample = samples.get(i);
            for (short value : sample.data()) {
                writeShort(smpl, value);
            }
            for (int pad = 0; pad < SAMPLE_PADDING; pad++) {
                writeShort(smpl, 0);
            }
            int[] bounds = sampleBoundOverrides.getOrDefault(i, new int[]{offset, offset + sample.data().length});
            writeName(shdr, sample.name());
            writeInt(shdr, bounds[0]);
            writeInt(shdr, bounds[1]);
            writeInt(shdr, offset + sample.loopStart());
            writeInt(shdr, offset + sample.loopEnd());
            writeInt(shdr, sample.sampleRate());
            shdr.write(sample.originalPitch());
            shdr.write(0);
            writeShort(shdr, 0);
            writeShort(shdr, sample.sampleType());
            offset += sample.data().length + SAMPLE_PADDING;
        }
        shdr.writeBytes(new byte[46]);

        ByteArrayOutputStream inst = new ByteArrayOutputStream();
        ByteArrayOutputStream ibag = new ByteArrayOutputStream();
        ByteArrayOutputStream igen = new ByteArrayOutputStream();
        writeZones(instruments, inst, ibag, igen, false);

        ByteArrayOutputStream phdr = new ByteArrayOutputStream();
        ByteArrayOutputStream pbag = new ByteArrayOutputStream();
        ByteArrayOutputStream pgen = new ByteArrayOutputStream();
        writeZones(presets, phdr, pbag, pgen, true);

        ByteArrayOutputStream info = new ByteArrayOutputStream();
        ByteArrayOutputStream ifil = new ByteArrayOutputStream();
        writeShort(ifil, ifilMajor);
        writeShort(ifil, 4);
        chunk(info, "ifil", ifil.toByteArray());
        chunk(info, "INAM", "Test\0\0".getBytes(StandardCharsets.US_ASCII));

        ByteArrayOutputStream sdta = new ByteArrayOutputStream();
        chunk(sdta, "smpl", smpl.toByteArray());

        ByteArrayOutputStream pdta = new ByteArrayOutputStream();
        chunk(pdta, "phdr", phdr.toByteArray());
        chunk(pdta, "pbag", pbag.toByteArray());
        chunk(pdta, "pmod", new byte[10]);
        chunk(pdta, "pgen", pgen.toByteArray());
        chunk(pdta, "inst", inst.toByteArray());
        chunk(pdta, "ibag", ibag.toByteArray());
        chunk(pdta, "imod", new byte[10]);
        chunk(pdta, "igen", igen.toByteArray());
        chunk(pdta, "shdr", shdr.toByteArray());

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.writeBytes(formType.getBytes(StandardCharsets.US_ASCII));
        list(body, "INFO", info.toByteArray());
        list(body, "sdta", sdta.toByteArray());
        list(body, "pdta", pdta.toByteArray());

        ByteArrayOutputStream riff = new ByteArrayOutputStream();
        riff.writeBytes("RIFF".getBytes(StandardCharsets.US_ASCII));
        writeInt(riff, body.size());
        riff.writeBytes(body.toByteArray());
        return riff.toByteArray();
    }

    private void writeZones(List<Named> headers, ByteArrayOutputStream headerOut, ByteArrayOutputStream bagOut,
                            ByteArrayOutputStream genOut, boolean preset) {
        int bagIndex = 0;
        int genIndex = 0;
        for (Named header : headers) {
            writeName(headerOut, header.name());
            if (preset) {
                writeShort(headerOut, header.program());
                writeShort(headerOut, header.bank());
            }
            writeShort(headerOut, bagIndex);
            if (preset) {
                headerOut.writeBytes(new byte[12]);
            }
            for (List<Gen> zone : header.zones()) {
                writeShort(bagOut, genIndex);
                writeShort(bagOut, 0);
                List<Gen> ordered = zone.stream().sorted(Comparator.comparingInt(TestSoundFontBuilder::order)).toList();
                for (Gen gen : ordered) {
                    writeShort(genOut, gen.type().id());
                    writeShort(genOut, gen.value());
                    genIndex++;
                }
                bagIndex++;
            }
        }
        writeName(headerOut, preset ? "EOP" : "EOI");
        if (preset) {
            writeShort(headerOut, 0);
            writeShort(headerOut, 0);
        }
        writeShort(headerOut, bagIndex);
        if (preset) {
            headerOut.writeBytes(new byte[12]);
        }
        writeShort(bagOut, genIndex);
        writeShort(bagOut, 0);
        genOut.writeBytes(new byte[4]);
    }

    private static int order(Gen gen) {
        return switch (gen.type()) {
            case KEY_RANGE -> 0;
            case VEL_RANGE -> 1;
            case INSTRUMENT, SAMPLE_ID -> 3;
            default -> 2;
        };
    }

    private void chunk(ByteArrayOutputStream out, String id, byte[] data) {
        if (omittedChunks.contains(id)) {
            return;
        }
        byte[] payload = paddedChunks.contains(id) ? Arrays.copyOf(data, data.length + 1) : data;
        out.writeBytes(id.getBytes(StandardCharsets.US_ASCII));
        writeInt(out, payload.length);
        out.writeBytes(payload);
        if ((payload.length & 1) == 1) {
            out.write(0);
        }
    }

    private void list(ByteArrayOutputStream out, String type, byte[] data) {
        if (omittedChunks.contains(type)) {
            return;
        }
        out.writeBytes("LIST".getBytes(StandardCharsets.US_ASCII));
        writeInt(out, data.length + 4);
        out.writeBytes(type.getBytes(StandardCharsets.US_ASCII));
        out.writeBytes(data);
    }

    private static void writeName(ByteArrayOutputStream out, String name) {
        out.writeBytes(Arrays.copyOf(name.getBytes(StandardCharsets.US_ASCII), 20));
    }

    private static void writeShort(ByteArrayOutputStream out, int value) {
        out.writeBytes(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) value).array());
    }

    private static void writeInt(ByteArrayOutputStream out, int value) {
        out.writeBytes(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array());
    }
}
```

`src/test/java/vn/ktt/music/infrastructure/synth/soundfont/SoundFontLoaderTest.java`:

```java
package vn.ktt.music.infrastructure.synth.soundfont;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.ktt.music.infrastructure.synth.TestSoundFontBuilder;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vn.ktt.music.infrastructure.synth.TestSoundFontBuilder.gen;
import static vn.ktt.music.infrastructure.synth.TestSoundFontBuilder.range;
import static vn.ktt.music.infrastructure.synth.TestSoundFontBuilder.zone;

class SoundFontLoaderTest {

    @TempDir
    Path tempDir;

    private TestSoundFontBuilder twoZoneFont() {
        TestSoundFontBuilder builder = new TestSoundFontBuilder();
        int left = builder.addSample("left", TestSoundFontBuilder.ramp(100, 10), 44100, 60, 4, 10, 90);
        int right = builder.addSample("right", TestSoundFontBuilder.constant(50, 1000), 22050, 61, 2, 0, 0);
        int instrument = builder.addInstrument("piano", List.of(
                zone(gen(GeneratorType.RELEASE_VOL_ENV, -386)),
                zone(gen(GeneratorType.KEY_RANGE, range(60, 72)), gen(GeneratorType.PAN, -500),
                        gen(GeneratorType.SAMPLE_ID, left)),
                zone(gen(GeneratorType.KEY_RANGE, range(60, 72)), gen(GeneratorType.PAN, 500),
                        gen(GeneratorType.SAMPLE_ID, right))));
        builder.addPreset("Piano", 0, 0, List.of(
                zone(gen(GeneratorType.INITIAL_ATTENUATION, 20)),
                zone(gen(GeneratorType.VEL_RANGE, range(64, 127)), gen(GeneratorType.INSTRUMENT, instrument))));
        builder.addPreset("Other", 8, 5, List.of(zone(gen(GeneratorType.INSTRUMENT, instrument))));
        return builder;
    }

    @Test
    void parsesPresetsZonesAndSamples() {
        SoundFont font = SoundFontLoader.load(twoZoneFont().writeTo(tempDir));

        assertEquals(2, font.presets().size());
        Preset piano = font.findPreset(0, 0).orElseThrow();
        assertEquals("Piano", piano.name());
        assertEquals(20, piano.globalZone().get(GeneratorType.INITIAL_ATTENUATION));
        assertEquals(1, piano.zones().size());
        assertEquals(range(64, 127), piano.zones().getFirst().zone().get(GeneratorType.VEL_RANGE));

        Instrument instrument = piano.zones().getFirst().instrument();
        assertEquals("piano", instrument.name());
        assertEquals(-386, instrument.globalZone().get(GeneratorType.RELEASE_VOL_ENV));
        assertEquals(2, instrument.zones().size());
        assertEquals(-500, instrument.zones().get(0).zone().get(GeneratorType.PAN));
        assertEquals(500, instrument.zones().get(1).zone().get(GeneratorType.PAN));
        assertSame(instrument, font.findPreset(8, 5).orElseThrow().zones().getFirst().instrument());
        assertTrue(font.findPreset(0, 1).isEmpty());
    }

    @Test
    void parsesSampleHeadersAndData() {
        SoundFont font = SoundFontLoader.load(twoZoneFont().writeTo(tempDir));
        List<InstrumentZone> zones = font.findPreset(0, 0).orElseThrow().zones().getFirst().instrument().zones();

        SampleHeader left = zones.get(0).sample();
        assertEquals(new SampleHeader("left", 0, 100, 10, 90, 44100, 60, 0, 4), left);
        SampleHeader right = zones.get(1).sample();
        assertEquals(new SampleHeader("right", 146, 196, 146, 146, 22050, 61, 0, 2), right);

        assertEquals(30, font.sampleData().get(left.start() + 3));
        assertEquals(1000, font.sampleData().get(right.start()));
    }

    @Test
    void unsetGeneratorsReturnDefaults() {
        Zone zone = new Zone(java.util.Map.of());
        assertFalse(zone.has(GeneratorType.INITIAL_FILTER_FC));
        assertEquals(13500, zone.get(GeneratorType.INITIAL_FILTER_FC));
        assertEquals(range(0, 127), zone.get(GeneratorType.KEY_RANGE));
        assertEquals(-1, zone.get(GeneratorType.OVERRIDING_ROOT_KEY));
    }

    @Test
    void rejectsWrongFormType() {
        Path file = twoZoneFont().formType("WAVE").writeTo(tempDir);
        assertThrows(SoundFontFormatException.class, () -> SoundFontLoader.load(file));
    }

    @Test
    void rejectsUnsupportedVersion() {
        Path file = twoZoneFont().ifilMajor(3).writeTo(tempDir);
        SoundFontFormatException error = assertThrows(SoundFontFormatException.class, () -> SoundFontLoader.load(file));
        assertTrue(error.getMessage().contains("version 3"));
    }

    @Test
    void rejectsMissingPdtaChunk() {
        Path file = twoZoneFont().omitChunk("pdta").writeTo(tempDir);
        assertThrows(SoundFontFormatException.class, () -> SoundFontLoader.load(file));
    }

    @Test
    void rejectsMissingSampleChunk() {
        Path file = twoZoneFont().omitChunk("smpl").writeTo(tempDir);
        SoundFontFormatException error = assertThrows(SoundFontFormatException.class, () -> SoundFontLoader.load(file));
        assertTrue(error.getMessage().contains("smpl"));
    }

    @Test
    void rejectsTruncatedRecord() {
        Path file = twoZoneFont().padChunk("pbag").writeTo(tempDir);
        SoundFontFormatException error = assertThrows(SoundFontFormatException.class, () -> SoundFontLoader.load(file));
        assertTrue(error.getMessage().contains("pbag"));
    }

    @Test
    void rejectsSampleBoundsOutsideSampleChunk() {
        Path file = twoZoneFont().overrideSampleBounds(1, 146, 100_000).writeTo(tempDir);
        SoundFontFormatException error = assertThrows(SoundFontFormatException.class, () -> SoundFontLoader.load(file));
        assertTrue(error.getMessage().contains("right"));
    }

    @Test
    void rejectsMissingFile() {
        assertThrows(SoundFontFormatException.class, () -> SoundFontLoader.load(tempDir.resolve("absent.sf2")));
    }

    @Test
    void rejectsAZoneThatReferencesAMissingInstrument() {
        TestSoundFontBuilder builder = new TestSoundFontBuilder();
        int sample = builder.addSample("s", TestSoundFontBuilder.constant(10, 1), 44100, 60, 1, 0, 0);
        builder.addInstrument("i", List.of(zone(gen(GeneratorType.SAMPLE_ID, sample))));
        builder.addPreset("p", 0, 0, List.of(zone(gen(GeneratorType.INSTRUMENT, 5))));
        Path file = builder.writeTo(tempDir);

        SoundFontFormatException error = assertThrows(SoundFontFormatException.class, () -> SoundFontLoader.load(file));
        assertTrue(error.getMessage().contains("instrument 5"));
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn test -Dtest=SoundFontLoaderTest`
Expected: `COMPILATION ERROR`, with `cannot find symbol` for `GeneratorType`, `SoundFontLoader` and `SoundFont`.

- [ ] **Step 3: Write the model and loader**

`src/main/java/vn/ktt/music/infrastructure/synth/soundfont/SoundFontFormatException.java`:

```java
package vn.ktt.music.infrastructure.synth.soundfont;

public class SoundFontFormatException extends RuntimeException {

    public SoundFontFormatException(String message) {
        super(message);
    }

    public SoundFontFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/soundfont/GeneratorType.java`. The defaults and ranges come from SF2 2.04 §8.1.3. Generators without a range (addresses, indices, ranges and flags) are never clamped, and only ranged generators may appear at preset level:

```java
package vn.ktt.music.infrastructure.synth.soundfont;

/**
 * The SF2 2.04 generators this synthesizer reads (section 8.1.2), with their defaults and the
 * value ranges from section 8.1.3. Generator ids not listed here are parsed and ignored.
 */
public enum GeneratorType {
    START_ADDRS_OFFSET(0, 0, false),
    END_ADDRS_OFFSET(1, 0, false),
    STARTLOOP_ADDRS_OFFSET(2, 0, false),
    ENDLOOP_ADDRS_OFFSET(3, 0, false),
    START_ADDRS_COARSE_OFFSET(4, 0, false),
    INITIAL_FILTER_FC(8, 13500, 1500, 13500),
    INITIAL_FILTER_Q(9, 0, 0, 960),
    END_ADDRS_COARSE_OFFSET(12, 0, false),
    CHORUS_EFFECTS_SEND(15, 0, 0, 1000),
    REVERB_EFFECTS_SEND(16, 0, 0, 1000),
    PAN(17, 0, -500, 500),
    DELAY_VOL_ENV(33, -12000, -12000, 5000),
    ATTACK_VOL_ENV(34, -12000, -12000, 8000),
    HOLD_VOL_ENV(35, -12000, -12000, 5000),
    DECAY_VOL_ENV(36, -12000, -12000, 8000),
    SUSTAIN_VOL_ENV(37, 0, 0, 1440),
    RELEASE_VOL_ENV(38, -12000, -12000, 8000),
    KEYNUM_TO_VOL_ENV_HOLD(39, 0, -1200, 1200),
    KEYNUM_TO_VOL_ENV_DECAY(40, 0, -1200, 1200),
    INSTRUMENT(41, 0, false),
    KEY_RANGE(43, 0x7F00, false),
    VEL_RANGE(44, 0x7F00, false),
    STARTLOOP_ADDRS_COARSE_OFFSET(45, 0, false),
    KEYNUM(46, -1, false),
    VELOCITY(47, -1, false),
    INITIAL_ATTENUATION(48, 0, 0, 1440),
    ENDLOOP_ADDRS_COARSE_OFFSET(50, 0, false),
    COARSE_TUNE(51, 0, -120, 120),
    FINE_TUNE(52, 0, -99, 99),
    SAMPLE_ID(53, 0, false),
    SAMPLE_MODES(54, 0, false),
    SCALE_TUNING(56, 100, 0, 1200),
    EXCLUSIVE_CLASS(57, 0, false),
    OVERRIDING_ROOT_KEY(58, -1, false);

    private static final GeneratorType[] BY_ID = new GeneratorType[61];

    static {
        for (GeneratorType type : values()) {
            BY_ID[type.id] = type;
        }
    }

    private final int id;
    private final int defaultValue;
    private final int min;
    private final int max;
    private final boolean presetAllowed;

    /** A generator with a clamped range that may also appear, additively, at preset level. */
    GeneratorType(int id, int defaultValue, int min, int max) {
        this.id = id;
        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.presetAllowed = true;
    }

    /** A generator without a clamped range (addresses, indices, ranges, flags). */
    GeneratorType(int id, int defaultValue, boolean presetAllowed) {
        this.id = id;
        this.defaultValue = defaultValue;
        this.min = Integer.MIN_VALUE;
        this.max = Integer.MAX_VALUE;
        this.presetAllowed = presetAllowed;
    }

    /** Returns the generator for an SF2 id, or null when this synthesizer ignores that id. */
    public static GeneratorType fromId(int id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : null;
    }

    public int id() {
        return id;
    }

    public int defaultValue() {
        return defaultValue;
    }

    public boolean presetAllowed() {
        return presetAllowed;
    }

    public int clamp(int value) {
        return Math.clamp(value, min, max);
    }

    /** Index and range generators carry unsigned 16-bit amounts. */
    boolean unsignedAmount() {
        return this == INSTRUMENT || this == SAMPLE_ID || this == KEY_RANGE || this == VEL_RANGE;
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/soundfont/Zone.java`:

```java
package vn.ktt.music.infrastructure.synth.soundfont;

import java.util.EnumMap;
import java.util.Map;

/** The generators set on one preset or instrument zone. Unset generators are absent, not defaulted. */
public final class Zone {

    public static final Zone EMPTY = new Zone(Map.of());

    private final EnumMap<GeneratorType, Integer> values = new EnumMap<>(GeneratorType.class);

    public Zone(Map<GeneratorType, Integer> values) {
        this.values.putAll(values);
    }

    public boolean has(GeneratorType type) {
        return values.containsKey(type);
    }

    /** The raw value, or the generator's SF2 default when it is not set on this zone. */
    public int get(GeneratorType type) {
        return values.getOrDefault(type, type.defaultValue());
    }

    public static int rangeLow(int rangeAmount) {
        return rangeAmount & 0xFF;
    }

    public static int rangeHigh(int rangeAmount) {
        return (rangeAmount >> 8) & 0xFF;
    }

    public static int range(int low, int high) {
        return (low & 0xFF) | ((high & 0xFF) << 8);
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/soundfont/SampleHeader.java`:

```java
package vn.ktt.music.infrastructure.synth.soundfont;

/** An SF2 sample header. Addresses are indices into the smpl chunk, in samples. */
public record SampleHeader(String name, int start, int end, int loopStart, int loopEnd, int sampleRate,
                           int originalPitch, int pitchCorrection, int sampleType) {

    private static final int ROM_FLAG = 0x8000;

    public boolean isRom() {
        return (sampleType & ROM_FLAG) != 0;
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/soundfont/InstrumentZone.java`:

```java
package vn.ktt.music.infrastructure.synth.soundfont;

public record InstrumentZone(Zone zone, SampleHeader sample) {
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/soundfont/Instrument.java`:

```java
package vn.ktt.music.infrastructure.synth.soundfont;

import java.util.List;

public record Instrument(String name, Zone globalZone, List<InstrumentZone> zones) {

    public Instrument {
        globalZone = globalZone == null ? Zone.EMPTY : globalZone;
        zones = List.copyOf(zones);
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/soundfont/PresetZone.java`:

```java
package vn.ktt.music.infrastructure.synth.soundfont;

public record PresetZone(Zone zone, Instrument instrument) {
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/soundfont/Preset.java`:

```java
package vn.ktt.music.infrastructure.synth.soundfont;

import java.util.List;

public record Preset(String name, int bank, int program, Zone globalZone, List<PresetZone> zones) {

    public Preset {
        globalZone = globalZone == null ? Zone.EMPTY : globalZone;
        zones = List.copyOf(zones);
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/soundfont/SoundFont.java`:

```java
package vn.ktt.music.infrastructure.synth.soundfont;

import java.nio.ShortBuffer;
import java.util.List;
import java.util.Optional;

/**
 * A parsed SoundFont. The sample data is shared between renders; readers must only use absolute
 * {@code get(int)} so that concurrent renders never touch the buffer's position.
 */
public final class SoundFont {

    private final List<Preset> presets;
    private final ShortBuffer sampleData;

    public SoundFont(List<Preset> presets, ShortBuffer sampleData) {
        this.presets = List.copyOf(presets);
        this.sampleData = sampleData;
    }

    public Optional<Preset> findPreset(int bank, int program) {
        return presets.stream()
                .filter(preset -> preset.bank() == bank && preset.program() == program)
                .findFirst();
    }

    public List<Preset> presets() {
        return presets;
    }

    public ShortBuffer sampleData() {
        return sampleData;
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/soundfont/SoundFontLoader.java`. The record sizes are 38 (`phdr`), 4 (`pbag`/`ibag`/`pgen`/`igen`), 10 (`pmod`/`imod`), 22 (`inst`) and 46 (`shdr`) bytes. The mapped buffer stays valid after the channel is closed:

```java
package vn.ktt.music.infrastructure.synth.soundfont;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads an SF2 file (SoundFont 2.04, section 4-7). The preset data is parsed onto the heap; the sample
 * chunk is memory-mapped read-only so large SoundFonts are paged in by the OS on demand.
 */
public final class SoundFontLoader {

    private static final int PHDR_SIZE = 38;
    private static final int BAG_SIZE = 4;
    private static final int MOD_SIZE = 10;
    private static final int GEN_SIZE = 4;
    private static final int INST_SIZE = 22;
    private static final int SHDR_SIZE = 46;
    private static final int NAME_LENGTH = 20;

    private SoundFontLoader() {
    }

    public static SoundFont load(Path path) {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            return read(channel);
        } catch (IOException e) {
            throw new SoundFontFormatException("Cannot read SoundFont " + path, e);
        }
    }

    private static SoundFont read(FileChannel channel) throws IOException {
        ByteBuffer header = readAt(channel, 0, 12);
        if (!"RIFF".equals(fourCc(header, 0)) || !"sfbk".equals(fourCc(header, 8))) {
            throw new SoundFontFormatException("Not an SF2 file: expected RIFF form 'sfbk'");
        }
        long riffEnd = Math.min(channel.size(), 8L + Integer.toUnsignedLong(header.getInt(4)));

        Map<String, ByteBuffer> chunks = new HashMap<>();
        long smplOffset = -1;
        long smplSize = 0;
        for (long position = 12; position + 8 <= riffEnd; ) {
            ByteBuffer chunkHeader = readAt(channel, position, 8);
            long size = Integer.toUnsignedLong(chunkHeader.getInt(4));
            if ("LIST".equals(fourCc(chunkHeader, 0))) {
                String listType = fourCc(readAt(channel, position + 8, 4), 0);
                long listEnd = Math.min(riffEnd, position + 8 + size);
                for (long sub = position + 12; sub + 8 <= listEnd; ) {
                    ByteBuffer subHeader = readAt(channel, sub, 8);
                    String id = fourCc(subHeader, 0);
                    long subSize = Integer.toUnsignedLong(subHeader.getInt(4));
                    if ("sdta".equals(listType) && "smpl".equals(id)) {
                        smplOffset = sub + 8;
                        smplSize = subSize;
                    } else if ("pdta".equals(listType) || "ifil".equals(id)) {
                        chunks.put(id, readAt(channel, sub + 8, Math.toIntExact(subSize)));
                    }
                    sub += 8 + subSize + (subSize & 1);
                }
            }
            position += 8 + size + (size & 1);
        }

        checkVersion(require(chunks, "ifil"));
        if (smplOffset < 0) {
            throw new SoundFontFormatException("Missing chunk 'smpl'");
        }
        if (smplSize > Integer.MAX_VALUE || smplOffset + smplSize > channel.size()) {
            throw new SoundFontFormatException("Chunk 'smpl' is too large or truncated");
        }
        MappedByteBuffer mapped = channel.map(FileChannel.MapMode.READ_ONLY, smplOffset, smplSize);
        mapped.order(ByteOrder.LITTLE_ENDIAN);
        ShortBuffer sampleData = mapped.asShortBuffer();

        List<SampleHeader> samples = readSamples(require(chunks, "shdr"), sampleData.capacity());
        records(require(chunks, "pmod"), MOD_SIZE, "pmod");
        records(require(chunks, "imod"), MOD_SIZE, "imod");
        List<Instrument> instruments = readInstruments(chunks, samples);
        List<Preset> presets = readPresets(chunks, instruments);
        return new SoundFont(presets, sampleData);
    }

    private static void checkVersion(ByteBuffer ifil) {
        if (ifil.capacity() < 4) {
            throw new SoundFontFormatException("Chunk 'ifil' is truncated");
        }
        int major = Short.toUnsignedInt(ifil.getShort(0));
        if (major != 2) {
            throw new SoundFontFormatException("Unsupported SoundFont version " + major + "; expected 2");
        }
    }

    private static List<SampleHeader> readSamples(ByteBuffer shdr, int smplSamples) {
        int count = records(shdr, SHDR_SIZE, "shdr") - 1;
        List<SampleHeader> samples = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int base = i * SHDR_SIZE;
            SampleHeader sample = new SampleHeader(
                    name(shdr, base),
                    shdr.getInt(base + 20),
                    shdr.getInt(base + 24),
                    shdr.getInt(base + 28),
                    shdr.getInt(base + 32),
                    shdr.getInt(base + 36),
                    Byte.toUnsignedInt(shdr.get(base + 40)),
                    shdr.get(base + 41),
                    Short.toUnsignedInt(shdr.getShort(base + 44)));
            boolean inBounds = sample.start() >= 0 && sample.start() <= sample.end() && sample.end() <= smplSamples;
            if (!sample.isRom() && !inBounds) {
                throw new SoundFontFormatException("Sample '" + sample.name() + "' bounds fall outside 'smpl'");
            }
            samples.add(sample);
        }
        return samples;
    }

    private static List<Instrument> readInstruments(Map<String, ByteBuffer> chunks, List<SampleHeader> samples) {
        ByteBuffer inst = require(chunks, "inst");
        ByteBuffer ibag = require(chunks, "ibag");
        ByteBuffer igen = require(chunks, "igen");
        int instRecords = records(inst, INST_SIZE, "inst");
        int bagRecords = records(ibag, BAG_SIZE, "ibag");
        int genRecords = records(igen, GEN_SIZE, "igen");

        List<Instrument> instruments = new ArrayList<>();
        for (int i = 0; i < instRecords - 1; i++) {
            int base = i * INST_SIZE;
            int bagFrom = Short.toUnsignedInt(inst.getShort(base + NAME_LENGTH));
            int bagTo = Short.toUnsignedInt(inst.getShort(base + INST_SIZE + NAME_LENGTH));
            checkSpan(bagFrom, bagTo, bagRecords - 1, "ibag");

            Zone global = null;
            List<InstrumentZone> zones = new ArrayList<>();
            for (int bag = bagFrom; bag < bagTo; bag++) {
                Zone zone = readZone(ibag, igen, bag, genRecords, "igen");
                if (zone.has(GeneratorType.SAMPLE_ID)) {
                    int sampleIndex = zone.get(GeneratorType.SAMPLE_ID);
                    if (sampleIndex >= samples.size()) {
                        throw new SoundFontFormatException("Instrument zone references missing sample " + sampleIndex);
                    }
                    zones.add(new InstrumentZone(zone, samples.get(sampleIndex)));
                } else if (bag == bagFrom) {
                    global = zone;
                }
            }
            instruments.add(new Instrument(name(inst, base), global, zones));
        }
        return instruments;
    }

    private static List<Preset> readPresets(Map<String, ByteBuffer> chunks, List<Instrument> instruments) {
        ByteBuffer phdr = require(chunks, "phdr");
        ByteBuffer pbag = require(chunks, "pbag");
        ByteBuffer pgen = require(chunks, "pgen");
        int presetRecords = records(phdr, PHDR_SIZE, "phdr");
        int bagRecords = records(pbag, BAG_SIZE, "pbag");
        int genRecords = records(pgen, GEN_SIZE, "pgen");

        List<Preset> presets = new ArrayList<>();
        for (int i = 0; i < presetRecords - 1; i++) {
            int base = i * PHDR_SIZE;
            int bagFrom = Short.toUnsignedInt(phdr.getShort(base + 24));
            int bagTo = Short.toUnsignedInt(phdr.getShort(base + PHDR_SIZE + 24));
            checkSpan(bagFrom, bagTo, bagRecords - 1, "pbag");

            Zone global = null;
            List<PresetZone> zones = new ArrayList<>();
            for (int bag = bagFrom; bag < bagTo; bag++) {
                Zone zone = readZone(pbag, pgen, bag, genRecords, "pgen");
                if (zone.has(GeneratorType.INSTRUMENT)) {
                    int instrumentIndex = zone.get(GeneratorType.INSTRUMENT);
                    if (instrumentIndex >= instruments.size()) {
                        throw new SoundFontFormatException("Preset zone references missing instrument " + instrumentIndex);
                    }
                    zones.add(new PresetZone(zone, instruments.get(instrumentIndex)));
                } else if (bag == bagFrom) {
                    global = zone;
                }
            }
            int program = Short.toUnsignedInt(phdr.getShort(base + 20));
            int bank = Short.toUnsignedInt(phdr.getShort(base + 22));
            presets.add(new Preset(name(phdr, base), bank, program, global, zones));
        }
        return presets;
    }

    private static Zone readZone(ByteBuffer bags, ByteBuffer gens, int bag, int genRecords, String genChunk) {
        int genFrom = Short.toUnsignedInt(bags.getShort(bag * BAG_SIZE));
        int genTo = Short.toUnsignedInt(bags.getShort((bag + 1) * BAG_SIZE));
        checkSpan(genFrom, genTo, genRecords - 1, genChunk);

        Map<GeneratorType, Integer> values = new EnumMap<>(GeneratorType.class);
        for (int gen = genFrom; gen < genTo; gen++) {
            GeneratorType type = GeneratorType.fromId(Short.toUnsignedInt(gens.getShort(gen * GEN_SIZE)));
            if (type == null) {
                continue;
            }
            short amount = gens.getShort(gen * GEN_SIZE + 2);
            values.put(type, type.unsignedAmount() ? Short.toUnsignedInt(amount) : amount);
        }
        return new Zone(values);
    }

    private static void checkSpan(int from, int to, int limit, String chunk) {
        if (from > to || to > limit) {
            throw new SoundFontFormatException("Chunk '" + chunk + "' index out of range: " + from + ".." + to);
        }
    }

    /** Validates record alignment and returns the record count, including the terminal record. */
    private static int records(ByteBuffer chunk, int recordSize, String id) {
        if (chunk.capacity() % recordSize != 0 || chunk.capacity() < recordSize) {
            throw new SoundFontFormatException("Chunk '" + id + "' has a bad size: " + chunk.capacity());
        }
        return chunk.capacity() / recordSize;
    }

    private static ByteBuffer require(Map<String, ByteBuffer> chunks, String id) {
        ByteBuffer chunk = chunks.get(id);
        if (chunk == null) {
            throw new SoundFontFormatException("Missing chunk '" + id + "'");
        }
        return chunk;
    }

    private static ByteBuffer readAt(FileChannel channel, long position, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.hasRemaining()) {
            if (channel.read(buffer, position + buffer.position()) < 0) {
                throw new SoundFontFormatException("SoundFont is truncated at byte " + position);
            }
        }
        return buffer;
    }

    private static String fourCc(ByteBuffer buffer, int offset) {
        byte[] bytes = new byte[4];
        buffer.get(offset, bytes);
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    private static String name(ByteBuffer buffer, int offset) {
        byte[] bytes = new byte[NAME_LENGTH];
        buffer.get(offset, bytes);
        int length = 0;
        while (length < NAME_LENGTH && bytes[length] != 0) {
            length++;
        }
        return new String(bytes, 0, length, StandardCharsets.ISO_8859_1).trim();
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `mvn test -Dtest='SoundFontLoaderTest,SynthPackageIsolationTest'`
Expected: `Tests run: 12, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/vn/ktt/music/infrastructure/synth/soundfont src/test/java/vn/ktt/music/infrastructure/synth/TestSoundFontBuilder.java src/test/java/vn/ktt/music/infrastructure/synth/soundfont
git commit -F - <<'MSG'
feat(synth): parse SoundFont 2 files and memory-map their samples (#5)

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
MSG
```

---

### Task 3: SF2 units and the volume envelope

**Files:**
- Create: `src/main/java/vn/ktt/music/infrastructure/synth/engine/Units.java`, `src/main/java/vn/ktt/music/infrastructure/synth/engine/EnvelopeTimecents.java`, `src/main/java/vn/ktt/music/infrastructure/synth/engine/VolumeEnvelope.java`
- Test: `src/test/java/vn/ktt/music/infrastructure/synth/engine/UnitsTest.java`, `src/test/java/vn/ktt/music/infrastructure/synth/engine/VolumeEnvelopeTest.java`

**Interfaces:**
- Consumes: nothing from earlier tasks.
- Produces (package-private, `vn.ktt.music.infrastructure.synth.engine`):
  - `final class Units`: `static double timecentsToSeconds(double)`, `centibelsToAmplitude(double)`, `amplitudeToCentibels(double)`, `absoluteCentsToHz(double)`, `decibelsToGain(double)`, `concave(double)` and `velocityAttenuationCb(int)`, plus `static int msToFrames(long ms, int rate)` and `secondsToFrames(double, int)`
  - `record EnvelopeTimecents(int delay, int attack, int hold, int decay, int sustainCb, int release)`
  - `final class VolumeEnvelope`:
    - Constructor `(int delayFrames, int attackFrames, int holdFrames, double decayFramesPer1000Cb, double sustainCb, double releaseFramesPer1000Cb)`
    - `static VolumeEnvelope fromTimecents(EnvelopeTimecents, int sampleRate)`
    - `double next()`, `void release()` and `boolean finished()`

- [ ] **Step 1: Write the failing tests**

`src/test/java/vn/ktt/music/infrastructure/synth/engine/UnitsTest.java`:

```java
package vn.ktt.music.infrastructure.synth.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnitsTest {

    @Test
    void timecentsToSeconds() {
        assertEquals(1.0, Units.timecentsToSeconds(0), 1e-12);
        assertEquals(0.5, Units.timecentsToSeconds(-1200), 1e-12);
        assertEquals(0.000977, Units.timecentsToSeconds(-12000), 1e-6);
        assertEquals(0.8, Units.timecentsToSeconds(-386), 1e-3);
    }

    @Test
    void centibelsToAmplitudeAndBack() {
        assertEquals(1.0, Units.centibelsToAmplitude(0), 1e-12);
        assertEquals(0.1, Units.centibelsToAmplitude(200), 1e-12);
        assertEquals(60.206, Units.amplitudeToCentibels(0.5), 1e-3);
    }

    @Test
    void absoluteCentsToHz() {
        assertEquals(19912, Units.absoluteCentsToHz(13500), 1.0);
        assertEquals(440.0, Units.absoluteCentsToHz(6900), 0.05);
        assertEquals(8000, Units.absoluteCentsToHz(11921), 5.0);
    }

    @Test
    void decibelsToGain() {
        assertEquals(1.0, Units.decibelsToGain(0), 1e-12);
        assertEquals(0.5012, Units.decibelsToGain(-6), 1e-4);
    }

    @Test
    void velocityAttenuationFollowsTheConcaveCurve() {
        assertEquals(0.0, Units.velocityAttenuationCb(127), 1e-9);
        assertEquals(59.8, Units.velocityAttenuationCb(90), 0.1);
        assertEquals(842, Units.velocityAttenuationCb(1), 1.0);
    }

    @Test
    void millisecondsToFrames() {
        assertEquals(44100, Units.msToFrames(1000, 44100));
        assertEquals(8291, Units.msToFrames(188, 44100));
        assertEquals(0, Units.msToFrames(0, 44100));
    }
}
```

`src/test/java/vn/ktt/music/infrastructure/synth/engine/VolumeEnvelopeTest.java`. The state machine is tested in frames; the timecent conversion is tested separately at a 1 kHz rate:

```java
package vn.ktt.music.infrastructure.synth.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VolumeEnvelopeTest {

    private static double[] run(VolumeEnvelope envelope, int frames) {
        double[] amplitudes = new double[frames];
        for (int i = 0; i < frames; i++) {
            amplitudes[i] = envelope.next();
        }
        return amplitudes;
    }

    @Test
    void delayThenLinearAttackThenHold() {
        double[] amplitudes = run(new VolumeEnvelope(2, 4, 3, 100, 200, 50), 9);

        assertEquals(0.0, amplitudes[0]);
        assertEquals(0.0, amplitudes[1]);
        assertEquals(0.0, amplitudes[2], 1e-12);
        assertEquals(0.25, amplitudes[3], 1e-12);
        assertEquals(0.5, amplitudes[4], 1e-12);
        assertEquals(0.75, amplitudes[5], 1e-12);
        assertEquals(1.0, amplitudes[6]);
        assertEquals(1.0, amplitudes[8]);
    }

    @Test
    void decayIsLinearInCentibelsAndReachesSustain() {
        double[] amplitudes = run(new VolumeEnvelope(2, 4, 3, 100, 200, 50), 120);

        assertEquals(1.0, amplitudes[9], 1e-12);
        assertEquals(Units.centibelsToAmplitude(10), amplitudes[10], 1e-12);
        assertEquals(Units.centibelsToAmplitude(190), amplitudes[28], 1e-12);
        assertEquals(0.1, amplitudes[29], 1e-12);
        assertEquals(0.1, amplitudes[119], 1e-12);
    }

    @Test
    void releaseFromSustainTakesTheRemainingCentibels() {
        VolumeEnvelope envelope = new VolumeEnvelope(2, 4, 3, 100, 200, 50);
        run(envelope, 40);

        envelope.release();
        assertEquals(0.1, envelope.next(), 1e-12);
        // (960 - 200) cB at 1000 cB per 50 frames = 38 frames in total
        run(envelope, 36);
        assertFalse(envelope.finished());
        envelope.next();
        assertTrue(envelope.finished());
        assertEquals(0.0, envelope.next());
    }

    @Test
    void releaseDuringAttackStartsFromTheCurrentLevel() {
        VolumeEnvelope envelope = new VolumeEnvelope(0, 10, 5, 100, 0, 100);
        run(envelope, 5);

        envelope.release();

        assertEquals(0.5, envelope.next(), 1e-9);
    }

    @Test
    void releaseDuringDelayFinishesImmediately() {
        VolumeEnvelope envelope = new VolumeEnvelope(10, 10, 5, 100, 0, 100);
        envelope.next();

        envelope.release();

        assertTrue(envelope.finished());
    }

    @Test
    void zeroLengthStagesAreSkipped() {
        double[] amplitudes = run(new VolumeEnvelope(0, 0, 0, 100, 0, 100), 3);

        assertEquals(1.0, amplitudes[0]);
        assertEquals(1.0, amplitudes[2]);
    }

    @Test
    void sustainAtSilenceFinishesTheVoiceDuringDecay() {
        VolumeEnvelope envelope = new VolumeEnvelope(0, 0, 0, 100, 1440, 100);
        // 960 cB at 10 cB per frame
        run(envelope, 95);
        assertFalse(envelope.finished());
        run(envelope, 1);
        assertTrue(envelope.finished());
    }

    @Test
    void fromTimecentsConvertsStageTimes() {
        // delay 0 tc = 1 s = 1000 frames; attack and hold -12000 tc round to 1 frame each at 1 kHz
        EnvelopeTimecents timecents = new EnvelopeTimecents(0, -12000, -12000, -12000, 0, -12000);
        double[] amplitudes = run(VolumeEnvelope.fromTimecents(timecents, 1000), 1003);

        assertEquals(0.0, amplitudes[999]);
        assertEquals(0.0, amplitudes[1000]);
        assertEquals(1.0, amplitudes[1001]);
        assertEquals(1.0, amplitudes[1002]);
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn test -Dtest='UnitsTest,VolumeEnvelopeTest'`
Expected: `COMPILATION ERROR`, with `cannot find symbol` for `Units` and `VolumeEnvelope`.

- [ ] **Step 3: Write the implementation**

`src/main/java/vn/ktt/music/infrastructure/synth/engine/Units.java`:

```java
package vn.ktt.music.infrastructure.synth.engine;

/** SF2 unit conversions (SF2 2.04 section 8.1.1) and the default velocity-to-attenuation modulator (8.4.2). */
final class Units {

    private static final double ABSOLUTE_CENTS_REFERENCE_HZ = 8.176;
    private static final double VELOCITY_ATTENUATION_CB = 960.0;

    private Units() {
    }

    static double timecentsToSeconds(double timecents) {
        return Math.pow(2.0, timecents / 1200.0);
    }

    static double centibelsToAmplitude(double centibels) {
        return Math.pow(10.0, -centibels / 200.0);
    }

    static double amplitudeToCentibels(double amplitude) {
        return -200.0 * Math.log10(amplitude);
    }

    static double absoluteCentsToHz(double cents) {
        return ABSOLUTE_CENTS_REFERENCE_HZ * Math.pow(2.0, cents / 1200.0);
    }

    static double decibelsToGain(double decibels) {
        return Math.pow(10.0, decibels / 20.0);
    }

    /** The SF2 concave transform: 0 at x = 0, 1 at x = 1, steep near 1. */
    static double concave(double x) {
        if (x <= 0.0) {
            return 0.0;
        }
        if (x >= 1.0) {
            return 1.0;
        }
        return Math.clamp(-(5.0 / 12.0) * Math.log10(1.0 - x), 0.0, 1.0);
    }

    /** Default modulator 8.4.2: note-on velocity to initial attenuation, negative concave, 960 cB. */
    static double velocityAttenuationCb(int velocity) {
        return VELOCITY_ATTENUATION_CB * concave(1.0 - velocity / 127.0);
    }

    static int msToFrames(long milliseconds, int sampleRate) {
        return Math.toIntExact(Math.round(milliseconds * (double) sampleRate / 1000.0));
    }

    static int secondsToFrames(double seconds, int sampleRate) {
        return Math.toIntExact(Math.round(seconds * sampleRate));
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/engine/EnvelopeTimecents.java`:

```java
package vn.ktt.music.infrastructure.synth.engine;

/** A resolved volume envelope in SF2 units. Hold and decay already include the keynum scaling. */
record EnvelopeTimecents(int delay, int attack, int hold, int decay, int sustainCb, int release) {
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/engine/VolumeEnvelope.java`:

```java
package vn.ktt.music.infrastructure.synth.engine;

/**
 * The SF2 volume envelope (SF2 2.04 sections 8.1.2 and 9.1.7), advanced one output frame at a time.
 * The attack is linear in amplitude; decay and release are linear in centibels, with their times
 * defined as the time to change by 1000 cB (100 dB).
 */
final class VolumeEnvelope {

    static final double SILENCE_CB = 960.0;

    private enum Stage { DELAY, ATTACK, HOLD, DECAY, SUSTAIN, RELEASE, FINISHED }

    private final int delayFrames;
    private final int attackFrames;
    private final int holdFrames;
    private final double decayCbPerFrame;
    private final double sustainCb;
    private final double releaseCbPerFrame;

    private Stage stage;
    private int stageFrame;
    private double attenuationCb;

    VolumeEnvelope(int delayFrames, int attackFrames, int holdFrames, double decayFramesPer1000Cb,
                   double sustainCb, double releaseFramesPer1000Cb) {
        this.delayFrames = delayFrames;
        this.attackFrames = attackFrames;
        this.holdFrames = holdFrames;
        this.decayCbPerFrame = 1000.0 / Math.max(1.0, decayFramesPer1000Cb);
        this.sustainCb = Math.clamp(sustainCb, 0.0, SILENCE_CB);
        this.releaseCbPerFrame = 1000.0 / Math.max(1.0, releaseFramesPer1000Cb);
        enter(Stage.DELAY);
    }

    static VolumeEnvelope fromTimecents(EnvelopeTimecents envelope, int sampleRate) {
        return new VolumeEnvelope(
                Units.secondsToFrames(Units.timecentsToSeconds(envelope.delay()), sampleRate),
                Units.secondsToFrames(Units.timecentsToSeconds(envelope.attack()), sampleRate),
                Units.secondsToFrames(Units.timecentsToSeconds(envelope.hold()), sampleRate),
                Units.timecentsToSeconds(envelope.decay()) * sampleRate,
                envelope.sustainCb(),
                Units.timecentsToSeconds(envelope.release()) * sampleRate);
    }

    /** Returns this frame's amplitude (0..1) and advances to the next frame. */
    double next() {
        double amplitude = amplitude();
        advance();
        return amplitude;
    }

    /** Starts the release phase from the current level. Has no effect once releasing. */
    void release() {
        if (stage == Stage.RELEASE || stage == Stage.FINISHED) {
            return;
        }
        double amplitude = amplitude();
        attenuationCb = amplitude <= 0.0 ? SILENCE_CB : Math.min(SILENCE_CB, Units.amplitudeToCentibels(amplitude));
        stage = attenuationCb >= SILENCE_CB ? Stage.FINISHED : Stage.RELEASE;
        stageFrame = 0;
    }

    boolean finished() {
        return stage == Stage.FINISHED;
    }

    private double amplitude() {
        return switch (stage) {
            case DELAY, FINISHED -> 0.0;
            case ATTACK -> (double) stageFrame / attackFrames;
            case HOLD -> 1.0;
            case DECAY, SUSTAIN, RELEASE -> Units.centibelsToAmplitude(attenuationCb);
        };
    }

    private void advance() {
        stageFrame++;
        switch (stage) {
            case DELAY -> {
                if (stageFrame >= delayFrames) {
                    enter(Stage.ATTACK);
                }
            }
            case ATTACK -> {
                if (stageFrame >= attackFrames) {
                    enter(Stage.HOLD);
                }
            }
            case HOLD -> {
                if (stageFrame >= holdFrames) {
                    enter(Stage.DECAY);
                }
            }
            case DECAY -> {
                attenuationCb += decayCbPerFrame;
                if (attenuationCb >= sustainCb) {
                    attenuationCb = sustainCb;
                    enter(Stage.SUSTAIN);
                }
            }
            case RELEASE -> {
                attenuationCb += releaseCbPerFrame;
                if (attenuationCb >= SILENCE_CB) {
                    enter(Stage.FINISHED);
                }
            }
            case SUSTAIN, FINISHED -> {
            }
        }
    }

    /** Enters a stage, skipping straight past stages that last zero frames. */
    private void enter(Stage next) {
        stage = next;
        stageFrame = 0;
        if (stage == Stage.DELAY && delayFrames <= 0) {
            stage = Stage.ATTACK;
        }
        if (stage == Stage.ATTACK && attackFrames <= 0) {
            stage = Stage.HOLD;
        }
        if (stage == Stage.HOLD && holdFrames <= 0) {
            stage = Stage.DECAY;
        }
        if (stage == Stage.DECAY) {
            attenuationCb = 0.0;
            if (sustainCb <= 0.0) {
                stage = Stage.SUSTAIN;
            }
        }
        if (stage == Stage.SUSTAIN && attenuationCb >= SILENCE_CB) {
            stage = Stage.FINISHED;
        }
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `mvn test -Dtest='UnitsTest,VolumeEnvelopeTest'`
Expected: `Tests run: 14, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/vn/ktt/music/infrastructure/synth/engine src/test/java/vn/ktt/music/infrastructure/synth/engine
git commit -F - <<'MSG'
feat(synth): add SF2 unit conversions and the volume envelope (#5)

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
MSG
```

---

### Task 4: Low-pass filter and interpolator

**Files:**
- Create: `src/main/java/vn/ktt/music/infrastructure/synth/engine/LowPassFilter.java`, `src/main/java/vn/ktt/music/infrastructure/synth/engine/SampleSource.java`, `src/main/java/vn/ktt/music/infrastructure/synth/engine/Interpolator.java`
- Test: `src/test/java/vn/ktt/music/infrastructure/synth/engine/LowPassFilterTest.java`, `src/test/java/vn/ktt/music/infrastructure/synth/engine/InterpolatorTest.java`

**Interfaces:**
- Consumes: `Units.absoluteCentsToHz` (Task 3) and `Interpolation` (Task 1).
- Produces (package-private, `synth.engine`):
  - `final class LowPassFilter(int cutoffCents, int resonanceCb, int sampleRate)`, with `double process(double)` and `boolean bypassed()`, and `static final int BYPASS_CENTS = 13500`
  - `@FunctionalInterface interface SampleSource { double at(int index); }`
  - `final class Interpolator` with `static double interpolate(Interpolation, SampleSource, double position)`

- [ ] **Step 1: Write the failing tests**

`src/test/java/vn/ktt/music/infrastructure/synth/engine/LowPassFilterTest.java`. 11921 cents is about 8 kHz, the brightest velocity layer of the real piano:

```java
package vn.ktt.music.infrastructure.synth.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LowPassFilterTest {

    private static final int RATE = 44100;
    private static final int CUTOFF_8KHZ_CENTS = 11921;

    private static double rmsOfSine(LowPassFilter filter, double frequencyHz) {
        double sum = 0;
        int counted = 0;
        for (int i = 0; i < 20_000; i++) {
            double y = filter.process(Math.sin(2 * Math.PI * frequencyHz * i / RATE));
            if (i >= 2_000) {
                sum += y * y;
                counted++;
            }
        }
        return Math.sqrt(sum / counted);
    }

    @Test
    void passesDcWithUnityGain() {
        LowPassFilter filter = new LowPassFilter(CUTOFF_8KHZ_CENTS, 0, RATE);
        double y = 0;
        for (int i = 0; i < 2_000; i++) {
            y = filter.process(1.0);
        }
        assertEquals(1.0, y, 1e-3);
    }

    @Test
    void attenuatesAnOctaveAboveCutoffByAtLeastTenDecibels() {
        double passband = rmsOfSine(new LowPassFilter(CUTOFF_8KHZ_CENTS, 0, RATE), 500);
        double stopband = rmsOfSine(new LowPassFilter(CUTOFF_8KHZ_CENTS, 0, RATE), 16_000);

        double attenuationDb = 20 * Math.log10(passband / stopband);

        assertTrue(attenuationDb >= 10.0, "attenuation was " + attenuationDb + " dB");
    }

    @Test
    void bypassesAtTheDefaultCutoff() {
        LowPassFilter filter = new LowPassFilter(13500, 0, RATE);
        assertTrue(filter.bypassed());
        assertEquals(0.123, filter.process(0.123));
    }

    @Test
    void bypassesWhenCutoffIsNearNyquist() {
        // 12000 cents is about 8.4 kHz, above 0.45 x 16 kHz
        assertTrue(new LowPassFilter(12000, 0, 16000).bypassed());
        assertFalse(new LowPassFilter(12000, 0, RATE).bypassed());
    }
}
```

`src/test/java/vn/ktt/music/infrastructure/synth/engine/InterpolatorTest.java`:

```java
package vn.ktt.music.infrastructure.synth.engine;

import org.junit.jupiter.api.Test;
import vn.ktt.music.infrastructure.synth.api.Interpolation;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InterpolatorTest {

    private static final double[] RAMP = {0.0, 0.1, 0.2, 0.3};
    private static final SampleSource SOURCE = index -> index >= 0 && index < RAMP.length ? RAMP[index] : 0.0;

    @Test
    void linearReturnsExactSamplesAtWholePositions() {
        assertEquals(0.2, Interpolator.interpolate(Interpolation.LINEAR, SOURCE, 2.0), 1e-12);
    }

    @Test
    void linearReturnsMidpoints() {
        assertEquals(0.05, Interpolator.interpolate(Interpolation.LINEAR, SOURCE, 0.5), 1e-12);
        assertEquals(0.225, Interpolator.interpolate(Interpolation.LINEAR, SOURCE, 2.25), 1e-12);
    }

    @Test
    void linearBlendsTowardsZeroPastTheEnd() {
        assertEquals(0.15, Interpolator.interpolate(Interpolation.LINEAR, SOURCE, 3.5), 1e-12);
        assertEquals(0.0, Interpolator.interpolate(Interpolation.LINEAR, SOURCE, 5.0), 1e-12);
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn test -Dtest='LowPassFilterTest,InterpolatorTest'`
Expected: `COMPILATION ERROR`, with `cannot find symbol` for `LowPassFilter`, `SampleSource` and `Interpolator`.

- [ ] **Step 3: Write the implementation**

`src/main/java/vn/ktt/music/infrastructure/synth/engine/LowPassFilter.java`:

```java
package vn.ktt.music.infrastructure.synth.engine;

/**
 * The SF2 resonant low-pass filter: a 2-pole RBJ biquad (direct form I). Bypassed at the SF2 default
 * cutoff (13500 cents, about 20 kHz) or when the cutoff is too close to Nyquist to be meaningful.
 */
final class LowPassFilter {

    static final int BYPASS_CENTS = 13500;
    private static final double MAX_CUTOFF_RATIO = 0.45;

    private final boolean bypass;
    private final double b0;
    private final double b1;
    private final double b2;
    private final double a1;
    private final double a2;
    private double x1;
    private double x2;
    private double y1;
    private double y2;

    LowPassFilter(int cutoffCents, int resonanceCb, int sampleRate) {
        double cutoffHz = Units.absoluteCentsToHz(cutoffCents);
        bypass = cutoffCents >= BYPASS_CENTS || cutoffHz >= MAX_CUTOFF_RATIO * sampleRate;
        if (bypass) {
            b0 = 1.0;
            b1 = 0.0;
            b2 = 0.0;
            a1 = 0.0;
            a2 = 0.0;
            return;
        }
        double q = Math.pow(10.0, (resonanceCb / 10.0 - 3.01) / 20.0);
        double w0 = 2.0 * Math.PI * cutoffHz / sampleRate;
        double cos = Math.cos(w0);
        double alpha = Math.sin(w0) / (2.0 * q);
        double a0 = 1.0 + alpha;
        b0 = (1.0 - cos) / 2.0 / a0;
        b1 = (1.0 - cos) / a0;
        b2 = b0;
        a1 = -2.0 * cos / a0;
        a2 = (1.0 - alpha) / a0;
    }

    boolean bypassed() {
        return bypass;
    }

    double process(double x) {
        if (bypass) {
            return x;
        }
        double y = b0 * x + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2;
        x2 = x1;
        x1 = x;
        y2 = y1;
        y1 = y;
        return y;
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/engine/SampleSource.java`:

```java
package vn.ktt.music.infrastructure.synth.engine;

/** A voice's view of its sample data: the value at an absolute sample index, with looping and bounds applied. */
@FunctionalInterface
interface SampleSource {

    double at(int index);
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/engine/Interpolator.java`. The `switch` is on our own small enum, not a polymorphic DTO type, so it doesn't conflict with the registry rule in CLAUDE.md:

```java
package vn.ktt.music.infrastructure.synth.engine;

import vn.ktt.music.infrastructure.synth.api.Interpolation;

/** Reads a sample value at a fractional position. */
final class Interpolator {

    private Interpolator() {
    }

    static double interpolate(Interpolation mode, SampleSource source, double position) {
        return switch (mode) {
            case LINEAR -> linear(source, position);
        };
    }

    private static double linear(SampleSource source, double position) {
        int index = (int) Math.floor(position);
        double fraction = position - index;
        double current = source.at(index);
        if (fraction == 0.0) {
            return current;
        }
        return current + fraction * (source.at(index + 1) - current);
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `mvn test -Dtest='LowPassFilterTest,InterpolatorTest'`
Expected: `Tests run: 7, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/vn/ktt/music/infrastructure/synth/engine src/test/java/vn/ktt/music/infrastructure/synth/engine
git commit -F - <<'MSG'
feat(synth): add the resonant low-pass filter and linear interpolation (#5)

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
MSG
```

---

### Task 5: Zone resolution

**Files:**
- Create: `src/main/java/vn/ktt/music/infrastructure/synth/engine/VoiceSpec.java`, `src/main/java/vn/ktt/music/infrastructure/synth/engine/ZoneResolver.java`
- Test: `src/test/java/vn/ktt/music/infrastructure/synth/engine/ZoneResolverTest.java`

**Interfaces:**
- Consumes: the Task 2 model (`Preset`, `PresetZone`, `Instrument`, `InstrumentZone`, `Zone`, `SampleHeader`, `GeneratorType`), and `Units.velocityAttenuationCb` and `EnvelopeTimecents` (Task 3).
- Produces (package-private, `synth.engine`):
  - `record VoiceSpec(SampleHeader sample, int start, int end, int loopStart, int loopEnd, int sampleModes, int key, int velocity, double pitchCents, double attenuationCb, int filterCutoffCents, int filterResonanceCb, int pan, EnvelopeTimecents envelope)`
  - `final class ZoneResolver` with `static List<VoiceSpec> resolve(Preset preset, int key, int velocity)`

- [ ] **Step 1: Write the failing tests**

`src/test/java/vn/ktt/music/infrastructure/synth/engine/ZoneResolverTest.java`:

```java
package vn.ktt.music.infrastructure.synth.engine;

import org.junit.jupiter.api.Test;
import vn.ktt.music.infrastructure.synth.soundfont.GeneratorType;
import vn.ktt.music.infrastructure.synth.soundfont.Instrument;
import vn.ktt.music.infrastructure.synth.soundfont.InstrumentZone;
import vn.ktt.music.infrastructure.synth.soundfont.Preset;
import vn.ktt.music.infrastructure.synth.soundfont.PresetZone;
import vn.ktt.music.infrastructure.synth.soundfont.SampleHeader;
import vn.ktt.music.infrastructure.synth.soundfont.Zone;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vn.ktt.music.infrastructure.synth.soundfont.GeneratorType.*;

class ZoneResolverTest {

    private static final SampleHeader SAMPLE = new SampleHeader("s", 0, 100, 10, 90, 44100, 60, 0, 1);

    private static Zone zone(Object... typeValuePairs) {
        Map<GeneratorType, Integer> values = new EnumMap<>(GeneratorType.class);
        for (int i = 0; i < typeValuePairs.length; i += 2) {
            values.put((GeneratorType) typeValuePairs[i], (Integer) typeValuePairs[i + 1]);
        }
        return new Zone(values);
    }

    private static Preset preset(Zone presetGlobal, Zone presetLocal, Zone instrumentGlobal, Zone instrumentLocal,
                                 SampleHeader sample) {
        Instrument instrument = new Instrument("i", instrumentGlobal, List.of(new InstrumentZone(instrumentLocal, sample)));
        return new Preset("p", 0, 0, presetGlobal, List.of(new PresetZone(presetLocal, instrument)));
    }

    private static VoiceSpec only(Preset preset, int key, int velocity) {
        List<VoiceSpec> voices = ZoneResolver.resolve(preset, key, velocity);
        assertEquals(1, voices.size());
        return voices.getFirst();
    }

    @Test
    void instrumentValuesAreAbsoluteLocalThenGlobalThenDefault() {
        assertEquals(-200, only(preset(Zone.EMPTY, zone(), zone(PAN, 100), zone(PAN, -200), SAMPLE), 60, 127).pan());
        assertEquals(100, only(preset(Zone.EMPTY, zone(), zone(PAN, 100), zone(), SAMPLE), 60, 127).pan());
        assertEquals(0, only(preset(Zone.EMPTY, zone(), zone(), zone(), SAMPLE), 60, 127).pan());
    }

    @Test
    void presetValuesAreAddedLocalThenGlobal() {
        Preset local = preset(zone(INITIAL_ATTENUATION, 30), zone(INITIAL_ATTENUATION, 100),
                zone(), zone(INITIAL_ATTENUATION, 50), SAMPLE);
        assertEquals(150, only(local, 60, 127).attenuationCb(), 1e-9);

        Preset global = preset(zone(INITIAL_ATTENUATION, 30), zone(),
                zone(), zone(INITIAL_ATTENUATION, 50), SAMPLE);
        assertEquals(80, only(global, 60, 127).attenuationCb(), 1e-9);
    }

    @Test
    void summedValuesAreClampedToTheGeneratorRange() {
        Preset preset = preset(Zone.EMPTY, zone(INITIAL_FILTER_FC, 1000), zone(), zone(INITIAL_FILTER_FC, 13000), SAMPLE);
        assertEquals(13500, only(preset, 60, 127).filterCutoffCents());
    }

    @Test
    void keyAndVelocityRangesMustBothMatch() {
        Preset preset = preset(Zone.EMPTY, zone(VEL_RANGE, Zone.range(0, 63)),
                zone(), zone(KEY_RANGE, Zone.range(60, 60)), SAMPLE);

        assertEquals(1, ZoneResolver.resolve(preset, 60, 50).size());
        assertTrue(ZoneResolver.resolve(preset, 61, 50).isEmpty());
        assertTrue(ZoneResolver.resolve(preset, 60, 64).isEmpty());
    }

    @Test
    void globalZoneRangeAppliesWhenTheLocalZoneHasNone() {
        Preset preset = preset(Zone.EMPTY, zone(), zone(KEY_RANGE, Zone.range(10, 20)), zone(), SAMPLE);

        assertEquals(1, ZoneResolver.resolve(preset, 15, 100).size());
        assertTrue(ZoneResolver.resolve(preset, 60, 100).isEmpty());
    }

    @Test
    void twoMatchingInstrumentZonesGiveTwoVoices() {
        Instrument stereo = new Instrument("i", Zone.EMPTY, List.of(
                new InstrumentZone(zone(PAN, -500), SAMPLE),
                new InstrumentZone(zone(PAN, 500), SAMPLE)));
        Preset preset = new Preset("p", 0, 0, Zone.EMPTY, List.of(new PresetZone(zone(), stereo)));

        List<VoiceSpec> voices = ZoneResolver.resolve(preset, 60, 100);

        assertEquals(List.of(-500, 500), voices.stream().map(VoiceSpec::pan).toList());
    }

    @Test
    void generatorsInvalidAtPresetLevelAreIgnoredThere() {
        Preset preset = preset(Zone.EMPTY, zone(OVERRIDING_ROOT_KEY, 72, START_ADDRS_OFFSET, 5, SAMPLE_MODES, 1),
                zone(), zone(), SAMPLE);

        VoiceSpec voice = only(preset, 60, 127);

        assertEquals(0.0, voice.pitchCents(), 1e-9);
        assertEquals(0, voice.start());
        assertEquals(0, voice.sampleModes());
    }

    @Test
    void addressOffsetsMoveSamplePoints() {
        Preset preset = preset(Zone.EMPTY, zone(), zone(), zone(
                START_ADDRS_OFFSET, 5, END_ADDRS_OFFSET, -10, STARTLOOP_ADDRS_OFFSET, 2, ENDLOOP_ADDRS_OFFSET, -3), SAMPLE);

        VoiceSpec voice = only(preset, 60, 127);

        assertEquals(5, voice.start());
        assertEquals(90, voice.end());
        assertEquals(12, voice.loopStart());
        assertEquals(87, voice.loopEnd());
    }

    @Test
    void coarseOffsetsMoveBy32768Samples() {
        SampleHeader longSample = new SampleHeader("long", 0, 70_000, 0, 70_000, 44100, 60, 0, 1);
        Preset preset = preset(Zone.EMPTY, zone(), zone(), zone(START_ADDRS_COARSE_OFFSET, 1), longSample);

        assertEquals(32768, only(preset, 60, 127).start());
    }

    @Test
    void offsetsAreClampedToTheSampleBounds() {
        Preset preset = preset(Zone.EMPTY, zone(), zone(), zone(START_ADDRS_OFFSET, 200, ENDLOOP_ADDRS_OFFSET, -500), SAMPLE);

        VoiceSpec voice = only(preset, 60, 127);

        assertEquals(100, voice.start());
        assertEquals(100, voice.end());
        assertEquals(0, voice.loopEnd());
    }

    @Test
    void romSamplesProduceNoVoice() {
        SampleHeader rom = new SampleHeader("rom", 0, 100, 0, 0, 44100, 60, 0, 0x8001);

        assertTrue(ZoneResolver.resolve(preset(Zone.EMPTY, zone(), zone(), zone(), rom), 60, 100).isEmpty());
    }

    @Test
    void velocityAddsDefaultModulatorAttenuation() {
        Preset preset = preset(Zone.EMPTY, zone(), zone(), zone(), SAMPLE);

        assertEquals(0.0, only(preset, 60, 127).attenuationCb(), 1e-9);
        assertEquals(59.8, only(preset, 60, 90).attenuationCb(), 0.1);
        assertEquals(842, only(preset, 60, 1).attenuationCb(), 1.0);
    }

    @Test
    void pitchCombinesKeyTuningAndCorrection() {
        SampleHeader corrected = new SampleHeader("c", 0, 100, 0, 0, 44100, 60, 5, 1);
        Preset preset = preset(Zone.EMPTY, zone(COARSE_TUNE, 1), zone(), zone(FINE_TUNE, -20), corrected);

        // (72 - 60) x 100 + 100 x 1 - 20 + 5
        assertEquals(1285.0, only(preset, 72, 127).pitchCents(), 1e-9);
    }

    @Test
    void scaleTuningAndOverridingRootKeyChangeThePitch() {
        Preset preset = preset(Zone.EMPTY, zone(), zone(), zone(SCALE_TUNING, 50, OVERRIDING_ROOT_KEY, 70), SAMPLE);

        assertEquals(100.0, only(preset, 72, 127).pitchCents(), 1e-9);
    }

    @Test
    void invalidOriginalPitchFallsBackToMiddleC() {
        SampleHeader unpitched = new SampleHeader("u", 0, 100, 0, 0, 44100, 255, 0, 1);

        assertEquals(200.0, only(preset(Zone.EMPTY, zone(), zone(), zone(), unpitched), 62, 127).pitchCents(), 1e-9);
    }

    @Test
    void keynumAndVelocityGeneratorsOverrideTheNote() {
        Preset preset = preset(Zone.EMPTY, zone(), zone(), zone(KEYNUM, 64, VELOCITY, 127), SAMPLE);

        VoiceSpec voice = only(preset, 60, 1);

        assertEquals(64, voice.key());
        assertEquals(400.0, voice.pitchCents(), 1e-9);
        assertEquals(0.0, voice.attenuationCb(), 1e-9);
    }

    @Test
    void holdAndDecayIncludeKeynumScaling() {
        Preset preset = preset(Zone.EMPTY, zone(), zone(), zone(
                HOLD_VOL_ENV, -1200, KEYNUM_TO_VOL_ENV_HOLD, 100,
                DECAY_VOL_ENV, -2400, KEYNUM_TO_VOL_ENV_DECAY, -50), SAMPLE);

        EnvelopeTimecents envelope = only(preset, 72, 127).envelope();

        assertEquals(-2400, envelope.hold());
        assertEquals(-1800, envelope.decay());
    }

    @Test
    void envelopeDefaultsMatchTheSpecification() {
        EnvelopeTimecents envelope = only(preset(Zone.EMPTY, zone(), zone(), zone(), SAMPLE), 60, 127).envelope();

        assertEquals(new EnvelopeTimecents(-12000, -12000, -12000, -12000, 0, -12000), envelope);
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn test -Dtest=ZoneResolverTest`
Expected: `COMPILATION ERROR`, with `cannot find symbol` for `ZoneResolver` and `VoiceSpec`.

- [ ] **Step 3: Write the implementation**

`src/main/java/vn/ktt/music/infrastructure/synth/engine/VoiceSpec.java`:

```java
package vn.ktt.music.infrastructure.synth.engine;

import vn.ktt.music.infrastructure.synth.soundfont.SampleHeader;

/**
 * Everything one voice needs, resolved from a (preset zone, instrument zone) pair for one note.
 * Addresses are absolute indices into the SoundFont's sample data.
 */
record VoiceSpec(
        SampleHeader sample,
        int start,
        int end,
        int loopStart,
        int loopEnd,
        int sampleModes,
        int key,
        int velocity,
        double pitchCents,
        double attenuationCb,
        int filterCutoffCents,
        int filterResonanceCb,
        int pan,
        EnvelopeTimecents envelope) {
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/engine/ZoneResolver.java`. Instrument values are absolute and preset values are additive. A range set on a global zone applies to local zones that don't set their own. An `originalPitch` above 127 (the SF2 "unpitched" value 255) is treated as 60:

```java
package vn.ktt.music.infrastructure.synth.engine;

import vn.ktt.music.infrastructure.synth.soundfont.GeneratorType;
import vn.ktt.music.infrastructure.synth.soundfont.Instrument;
import vn.ktt.music.infrastructure.synth.soundfont.InstrumentZone;
import vn.ktt.music.infrastructure.synth.soundfont.Preset;
import vn.ktt.music.infrastructure.synth.soundfont.PresetZone;
import vn.ktt.music.infrastructure.synth.soundfont.SampleHeader;
import vn.ktt.music.infrastructure.synth.soundfont.Zone;

import java.util.ArrayList;
import java.util.List;

import static vn.ktt.music.infrastructure.synth.soundfont.GeneratorType.*;

/**
 * Turns a note into voices (SF2 2.04 section 9.4): one voice for every matching pair of preset zone and
 * instrument zone. Instrument generators are absolute (local, then global, then default); preset
 * generators are added on top (local, then global, otherwise 0).
 */
final class ZoneResolver {

    private static final int COARSE_OFFSET_UNIT = 32768;
    private static final int KEYNUM_SCALING_PIVOT = 60;
    private static final int DEFAULT_ROOT_KEY = 60;

    private ZoneResolver() {
    }

    static List<VoiceSpec> resolve(Preset preset, int key, int velocity) {
        List<VoiceSpec> voices = new ArrayList<>();
        for (PresetZone presetZone : preset.zones()) {
            if (!matches(presetZone.zone(), preset.globalZone(), key, velocity)) {
                continue;
            }
            Instrument instrument = presetZone.instrument();
            for (InstrumentZone instrumentZone : instrument.zones()) {
                if (!matches(instrumentZone.zone(), instrument.globalZone(), key, velocity)
                        || instrumentZone.sample().isRom()) {
                    continue;
                }
                Generators generators = new Generators(
                        presetZone.zone(), preset.globalZone(), instrumentZone.zone(), instrument.globalZone());
                voices.add(voice(generators, instrumentZone.sample(), key, velocity));
            }
        }
        return voices;
    }

    private static VoiceSpec voice(Generators g, SampleHeader sample, int noteKey, int noteVelocity) {
        int key = g.value(KEYNUM) >= 0 ? g.value(KEYNUM) : noteKey;
        int velocity = g.value(VELOCITY) > 0 ? g.value(VELOCITY) : noteVelocity;
        int rootKey = g.value(OVERRIDING_ROOT_KEY) >= 0
                ? g.value(OVERRIDING_ROOT_KEY)
                : sample.originalPitch() > 127 ? DEFAULT_ROOT_KEY : sample.originalPitch();
        double pitchCents = (key - rootKey) * (double) g.value(SCALE_TUNING)
                + 100.0 * g.value(COARSE_TUNE)
                + g.value(FINE_TUNE)
                + sample.pitchCorrection();

        int start = address(sample, sample.start(), g.value(START_ADDRS_OFFSET), g.value(START_ADDRS_COARSE_OFFSET));
        int end = Math.max(start,
                address(sample, sample.end(), g.value(END_ADDRS_OFFSET), g.value(END_ADDRS_COARSE_OFFSET)));
        int loopStart = address(sample, sample.loopStart(),
                g.value(STARTLOOP_ADDRS_OFFSET), g.value(STARTLOOP_ADDRS_COARSE_OFFSET));
        int loopEnd = address(sample, sample.loopEnd(),
                g.value(ENDLOOP_ADDRS_OFFSET), g.value(ENDLOOP_ADDRS_COARSE_OFFSET));

        int keyScaling = KEYNUM_SCALING_PIVOT - key;
        EnvelopeTimecents envelope = new EnvelopeTimecents(
                g.value(DELAY_VOL_ENV),
                g.value(ATTACK_VOL_ENV),
                g.value(HOLD_VOL_ENV) + g.value(KEYNUM_TO_VOL_ENV_HOLD) * keyScaling,
                g.value(DECAY_VOL_ENV) + g.value(KEYNUM_TO_VOL_ENV_DECAY) * keyScaling,
                g.value(SUSTAIN_VOL_ENV),
                g.value(RELEASE_VOL_ENV));

        return new VoiceSpec(
                sample, start, end, loopStart, loopEnd,
                g.value(SAMPLE_MODES) & 3,
                key, velocity, pitchCents,
                g.value(INITIAL_ATTENUATION) + Units.velocityAttenuationCb(velocity),
                g.value(INITIAL_FILTER_FC),
                g.value(INITIAL_FILTER_Q),
                g.value(PAN),
                envelope);
    }

    private static int address(SampleHeader sample, int base, int fineOffset, int coarseOffset) {
        long address = (long) base + fineOffset + (long) COARSE_OFFSET_UNIT * coarseOffset;
        return (int) Math.clamp(address, sample.start(), sample.end());
    }

    private static boolean matches(Zone local, Zone global, int key, int velocity) {
        int keyRange = local.has(KEY_RANGE) ? local.get(KEY_RANGE) : global.get(KEY_RANGE);
        int velRange = local.has(VEL_RANGE) ? local.get(VEL_RANGE) : global.get(VEL_RANGE);
        return key >= Zone.rangeLow(keyRange) && key <= Zone.rangeHigh(keyRange)
                && velocity >= Zone.rangeLow(velRange) && velocity <= Zone.rangeHigh(velRange);
    }

    private record Generators(Zone presetLocal, Zone presetGlobal, Zone instrumentLocal, Zone instrumentGlobal) {

        int value(GeneratorType type) {
            int instrumentValue = instrumentLocal.has(type) ? instrumentLocal.get(type)
                    : instrumentGlobal.has(type) ? instrumentGlobal.get(type)
                    : type.defaultValue();
            if (!type.presetAllowed()) {
                return instrumentValue;
            }
            int presetValue = presetLocal.has(type) ? presetLocal.get(type)
                    : presetGlobal.has(type) ? presetGlobal.get(type)
                    : 0;
            return type.clamp(instrumentValue + presetValue);
        }
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `mvn test -Dtest=ZoneResolverTest`
Expected: `Tests run: 18, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/vn/ktt/music/infrastructure/synth/engine src/test/java/vn/ktt/music/infrastructure/synth/engine
git commit -F - <<'MSG'
feat(synth): resolve SF2 preset and instrument zones into voices (#5)

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
MSG
```

---

### Task 6: Voice rendering

**Files:**
- Create: `src/main/java/vn/ktt/music/infrastructure/synth/engine/Voice.java`
- Test: `src/test/java/vn/ktt/music/infrastructure/synth/engine/VoiceTest.java`

**Interfaces:**
- Consumes: `VoiceSpec` (Task 5), `LowPassFilter`, `Interpolator` and `SampleSource` (Task 4), `VolumeEnvelope` and `Units` (Task 3), and `Interpolation` (Task 1).
- Produces (package-private, `synth.engine`): `final class Voice`, with:
  - Constructor `(VoiceSpec spec, ShortBuffer data, Interpolation interpolation, int sampleRate, int startFrame, int releaseFrame)`
  - `void renderInto(float[] left, float[] right)`
  - `static double playbackStep(VoiceSpec, int outputSampleRate)` and `static double[] panGains(int pan)`

- [ ] **Step 1: Write the failing tests**

`src/test/java/vn/ktt/music/infrastructure/synth/engine/VoiceTest.java`. At 1 kHz the shortest SF2 envelope reaches full level by frame 3, so the tests read frames from 10 onward:

```java
package vn.ktt.music.infrastructure.synth.engine;

import org.junit.jupiter.api.Test;
import vn.ktt.music.infrastructure.synth.api.Interpolation;
import vn.ktt.music.infrastructure.synth.soundfont.SampleHeader;

import java.nio.ShortBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VoiceTest {

    private static final int RATE = 1000;
    /** Shortest SF2 envelope: at 1 kHz delay, attack and hold last 1 frame each, so the level is 1 from frame 3. */
    private static final EnvelopeTimecents FAST = new EnvelopeTimecents(-12000, -12000, -12000, -12000, 0, -12000);

    private static VoiceSpec spec(SampleHeader sample, int sampleModes, double pitchCents, int pan,
                                  EnvelopeTimecents envelope) {
        return new VoiceSpec(sample, sample.start(), sample.end(), sample.loopStart(), sample.loopEnd(), sampleModes,
                60, 127, pitchCents, 0.0, 13500, 0, pan, envelope);
    }

    private static short[] ramp(int length) {
        short[] data = new short[length];
        for (int i = 0; i < length; i++) {
            data[i] = (short) (i * 1000);
        }
        return data;
    }

    private static float[] renderLeft(VoiceSpec spec, short[] data, int frames, int releaseFrame) {
        float[] left = new float[frames];
        float[] right = new float[frames];
        new Voice(spec, ShortBuffer.wrap(data), Interpolation.LINEAR, RATE, 0, releaseFrame).renderInto(left, right);
        return left;
    }

    @Test
    void stepIsOneWhenKeyEqualsRootAndRatesMatch() {
        SampleHeader sample = new SampleHeader("s", 0, 10, 0, 0, 44100, 60, 0, 1);
        assertEquals(1.0, Voice.playbackStep(spec(sample, 0, 0, 0, FAST), 44100), 1e-12);
    }

    @Test
    void stepDoublesOneOctaveUp() {
        SampleHeader sample = new SampleHeader("s", 0, 10, 0, 0, 44100, 60, 0, 1);
        assertEquals(2.0, Voice.playbackStep(spec(sample, 0, 1200, 0, FAST), 44100), 1e-12);
    }

    @Test
    void stepFollowsTheSampleRateRatio() {
        SampleHeader sample = new SampleHeader("s", 0, 10, 0, 0, 22050, 60, 0, 1);
        assertEquals(0.5, Voice.playbackStep(spec(sample, 0, 0, 0, FAST), 44100), 1e-12);
    }

    @Test
    void panGainsAreConstantPower() {
        assertArrayEquals(new double[]{1.0, 0.0}, Voice.panGains(-500), 1e-12);
        assertArrayEquals(new double[]{Math.sqrt(0.5), Math.sqrt(0.5)}, Voice.panGains(0), 1e-12);
        assertArrayEquals(new double[]{0.0, 1.0}, Voice.panGains(500), 1e-12);
    }

    @Test
    void unloopedVoiceStopsAtTheSampleEnd() {
        short[] data = ramp(20);
        SampleHeader sample = new SampleHeader("s", 0, 20, 0, 0, RATE, 60, 0, 1);

        float[] left = renderLeft(spec(sample, 0, 0, -500, FAST), data, 100, 100);

        assertEquals(10_000 / 32768.0, left[10], 1e-6);
        assertEquals(19_000 / 32768.0, left[19], 1e-6);
        assertEquals(0.0f, left[20]);
        assertEquals(0.0f, left[50]);
    }

    @Test
    void continuousLoopWrapsAroundTheLoopPoints() {
        short[] data = ramp(20);
        SampleHeader sample = new SampleHeader("s", 0, 20, 5, 15, RATE, 60, 0, 1);

        float[] left = renderLeft(spec(sample, 1, 0, -500, FAST), data, 100, 100);

        assertEquals(14_000 / 32768.0, left[14], 1e-6);
        assertEquals(5_000 / 32768.0, left[15], 1e-6);
        assertEquals(7_000 / 32768.0, left[17], 1e-6);
        assertEquals(5_000 / 32768.0, left[95], 1e-6);
    }

    @Test
    void loopUntilReleaseThenPlaysOnToTheEnd() {
        short[] data = ramp(40);
        SampleHeader sample = new SampleHeader("s", 0, 40, 5, 15, RATE, 60, 0, 1);
        // release 0 tc = 1 s = 1000 frames per 1000 cB, so the level falls by 1 cB per frame
        EnvelopeTimecents slowRelease = new EnvelopeTimecents(-12000, -12000, -12000, -12000, 0, 0);

        float[] left = renderLeft(spec(sample, 3, 0, -500, slowRelease), data, 100, 30);

        // at the release frame 30 the loop position is 5 + (30 - 15) % 10 = 10
        assertEquals(10_000 / 32768.0, left[30], 1e-6);
        // five frames later it has moved past the loop end without wrapping
        assertEquals(15_000 / 32768.0 * Units.centibelsToAmplitude(5), left[35], 1e-6);
        // and it stops at the sample end, frame 30 + (40 - 10)
        assertEquals(0.0f, left[60]);
    }

    @Test
    void attenuationScalesTheOutput() {
        short[] data = new short[50];
        java.util.Arrays.fill(data, (short) 16384);
        SampleHeader sample = new SampleHeader("s", 0, 50, 0, 0, RATE, 60, 0, 1);
        VoiceSpec attenuated = new VoiceSpec(sample, 0, 50, 0, 0, 0, 60, 127, 0, 200.0, 13500, 0, -500, FAST);

        float[] left = renderLeft(attenuated, data, 50, 50);

        assertEquals(0.05, left[10], 1e-6);
    }

    @Test
    void voiceStartsAtItsStartFrameAndEndsAfterRelease() {
        short[] data = new short[1000];
        java.util.Arrays.fill(data, (short) 16384);
        SampleHeader sample = new SampleHeader("s", 0, 1000, 0, 0, RATE, 60, 0, 1);
        float[] left = new float[200];
        float[] right = new float[200];

        new Voice(spec(sample, 0, 0, 0, FAST), ShortBuffer.wrap(data), Interpolation.LINEAR, RATE, 50, 100)
                .renderInto(left, right);

        assertEquals(0.0f, left[49]);
        assertEquals(0.5 * Math.sqrt(0.5), left[60], 1e-6);
        assertEquals(left[60], right[60], 1e-9);
        assertEquals(0.0f, left[150]);
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn test -Dtest=VoiceTest`
Expected: `COMPILATION ERROR`, with `cannot find symbol: class Voice`.

- [ ] **Step 3: Write the implementation**

`src/main/java/vn/ktt/music/infrastructure/synth/engine/Voice.java`:

```java
package vn.ktt.music.infrastructure.synth.engine;

import vn.ktt.music.infrastructure.synth.api.Interpolation;

import java.nio.ShortBuffer;

/**
 * One sounding sample: interpolated read, low-pass filter, volume envelope and attenuation, then
 * constant-power panning into the stereo mix buffers.
 */
final class Voice {

    private static final double SAMPLE_SCALE = 1.0 / 32768.0;
    private static final int LOOP_CONTINUOUS = 1;
    private static final int LOOP_UNTIL_RELEASE = 3;

    private final ShortBuffer data;
    private final int start;
    private final int end;
    private final int loopStart;
    private final int loopEnd;
    private final int sampleModes;
    private final boolean loopValid;
    private final double step;
    private final double gain;
    private final double leftGain;
    private final double rightGain;
    private final Interpolation interpolation;
    private final LowPassFilter filter;
    private final VolumeEnvelope envelope;
    private final int startFrame;
    private final int releaseFrame;

    private double position;
    private boolean released;
    private boolean ended;

    Voice(VoiceSpec spec, ShortBuffer data, Interpolation interpolation, int sampleRate, int startFrame, int releaseFrame) {
        this.data = data;
        this.start = spec.start();
        this.end = spec.end();
        this.loopStart = spec.loopStart();
        this.loopEnd = spec.loopEnd();
        this.sampleModes = spec.sampleModes();
        this.loopValid = loopEnd > loopStart;
        this.step = playbackStep(spec, sampleRate);
        this.gain = Units.centibelsToAmplitude(spec.attenuationCb());
        double[] pan = panGains(spec.pan());
        this.leftGain = pan[0];
        this.rightGain = pan[1];
        this.interpolation = interpolation;
        this.filter = new LowPassFilter(spec.filterCutoffCents(), spec.filterResonanceCb(), sampleRate);
        this.envelope = VolumeEnvelope.fromTimecents(spec.envelope(), sampleRate);
        this.startFrame = startFrame;
        this.releaseFrame = releaseFrame;
        this.position = start;
    }

    /** Sample frames to advance per output frame. */
    static double playbackStep(VoiceSpec spec, int outputSampleRate) {
        return Math.pow(2.0, spec.pitchCents() / 1200.0) * spec.sample().sampleRate() / outputSampleRate;
    }

    /** Constant-power pan gains {left, right} for an SF2 pan value (-500 = hard left, 500 = hard right). */
    static double[] panGains(int pan) {
        double position = Math.clamp(pan / 500.0, -1.0, 1.0);
        double angle = (position + 1.0) * Math.PI / 4.0;
        return new double[]{Math.cos(angle), Math.sin(angle)};
    }

    /** Adds this voice into the mix buffers from its start frame until it ends or the buffers end. */
    void renderInto(float[] left, float[] right) {
        for (int frame = startFrame; frame < left.length && !ended; frame++) {
            if (!released && frame >= releaseFrame) {
                envelope.release();
                released = true;
            }
            double sample = filter.process(Interpolator.interpolate(interpolation, this::sampleAt, position));
            double amplitude = envelope.next() * gain;
            left[frame] += (float) (sample * amplitude * leftGain);
            right[frame] += (float) (sample * amplitude * rightGain);
            advance();
            if (envelope.finished()) {
                ended = true;
            }
        }
    }

    private boolean looping() {
        return loopValid && (sampleModes == LOOP_CONTINUOUS || (sampleModes == LOOP_UNTIL_RELEASE && !released));
    }

    private double sampleAt(int index) {
        int resolved = index;
        if (looping() && resolved >= loopEnd) {
            resolved = loopStart + (resolved - loopEnd) % (loopEnd - loopStart);
        }
        if (resolved < start || resolved >= end) {
            return 0.0;
        }
        return data.get(resolved) * SAMPLE_SCALE;
    }

    private void advance() {
        position += step;
        if (looping()) {
            while (position >= loopEnd) {
                position -= loopEnd - loopStart;
            }
        } else if (position >= end) {
            ended = true;
        }
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `mvn test -Dtest=VoiceTest`
Expected: `Tests run: 9, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/vn/ktt/music/infrastructure/synth/engine/Voice.java src/test/java/vn/ktt/music/infrastructure/synth/engine/VoiceTest.java
git commit -F - <<'MSG'
feat(synth): render voices with looping, filter, envelope and panning (#5)

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
MSG
```

---

### Task 7: Mixer and the Synthesizer facade

**Files:**
- Create: `src/main/java/vn/ktt/music/infrastructure/synth/engine/Mixer.java`, `src/main/java/vn/ktt/music/infrastructure/synth/Synthesizer.java`
- Test: `src/test/java/vn/ktt/music/infrastructure/synth/engine/MixerTest.java`, `src/test/java/vn/ktt/music/infrastructure/synth/SynthesizerTest.java`, `src/test/java/vn/ktt/music/infrastructure/synth/GrandPianoSmokeTest.java`

**Interfaces:**
- Consumes: everything from Tasks 1–6, and `TestSoundFontBuilder` (Task 2) in the tests.
- Produces:
  - `public final class Mixer` (`synth.engine`): constructor `(SoundFont)`, `SynthOutput render(SynthRequest)`, `public static final long MAX_DURATION_MS = 600_000`, and the package-private `static double softLimit(double)`
  - `public final class Synthesizer` (`synth`): constructor `(SoundFont)` and `SynthOutput render(SynthRequest)`. **This is the only class the rest of the app uses.**

- [ ] **Step 1: Write the failing tests**

`src/test/java/vn/ktt/music/infrastructure/synth/engine/MixerTest.java`. In double precision `tanh` reaches exactly 1.0 for large inputs, so the guarantee is "never exceeds 1":

```java
package vn.ktt.music.infrastructure.synth.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MixerTest {

    @Test
    void softLimitPassesQuietSignalsUnchanged() {
        assertEquals(0.5, Mixer.softLimit(0.5));
        assertEquals(-0.8, Mixer.softLimit(-0.8));
    }

    @Test
    void softLimitNeverExceedsOne() {
        assertTrue(Mixer.softLimit(0.9) > 0.8 && Mixer.softLimit(0.9) < 0.9);
        assertTrue(Mixer.softLimit(1.5) < 1.0);
        assertTrue(Mixer.softLimit(5.0) <= 1.0);
        assertTrue(Mixer.softLimit(-5.0) >= -1.0);
    }

    @Test
    void softLimitIsMonotonic() {
        double previous = Mixer.softLimit(0.0);
        for (double x = 0.01; x < 2.0; x += 0.01) {
            double current = Mixer.softLimit(x);
            assertTrue(current > previous, "not increasing at " + x);
            previous = current;
        }
    }
}
```

`src/test/java/vn/ktt/music/infrastructure/synth/SynthesizerTest.java`. These run end to end on a fixture SoundFont: a looped constant 0.5 sample on key 60, centered, so each channel carries `0.5 × √0.5`:

```java
package vn.ktt.music.infrastructure.synth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.ktt.music.infrastructure.synth.api.ChannelLayout;
import vn.ktt.music.infrastructure.synth.api.NoteSpec;
import vn.ktt.music.infrastructure.synth.api.OutputFormat;
import vn.ktt.music.infrastructure.synth.api.PresetRef;
import vn.ktt.music.infrastructure.synth.api.SynthOptions;
import vn.ktt.music.infrastructure.synth.api.SynthOutput;
import vn.ktt.music.infrastructure.synth.api.SynthRequest;
import vn.ktt.music.infrastructure.synth.soundfont.GeneratorType;
import vn.ktt.music.infrastructure.synth.soundfont.SoundFontLoader;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vn.ktt.music.infrastructure.synth.TestSoundFontBuilder.gen;
import static vn.ktt.music.infrastructure.synth.TestSoundFontBuilder.range;

class SynthesizerTest {

    private static final double CENTER_GAIN = Math.sqrt(0.5);

    @TempDir
    Path tempDir;

    /** A looped constant 0.5 sample on key 60 only, centered, no filter, looping continuously. */
    private Synthesizer constantSynth() {
        Path file = TestSoundFontBuilder.singleZone(TestSoundFontBuilder.constant(44100, 16384),
                gen(GeneratorType.KEY_RANGE, range(60, 60)),
                gen(GeneratorType.SAMPLE_MODES, 1)).writeTo(tempDir);
        return new Synthesizer(SoundFontLoader.load(file));
    }

    private static SynthOutput render(Synthesizer synth, SynthOptions options, NoteSpec... notes) {
        return synth.render(new SynthRequest(List.of(notes), options));
    }

    @Test
    void bufferLastsUntilTheLastReleasePlusTheTail() {
        SynthOutput output = render(constantSynth(), SynthOptions.defaults(),
                new NoteSpec(60, 0, 100, 127), new NoteSpec(60, 300, 200, 127));

        assertEquals(44100, output.sampleRate());
        assertEquals(2, output.channels());
        assertEquals(Math.round((500 + 1000) * 44.1), output.frames());
    }

    @Test
    void steadyStateMatchesSampleLevelTimesPanGain() {
        SynthOutput output = render(constantSynth(), SynthOptions.defaults(), new NoteSpec(60, 0, 500, 127));

        int frame = 4410;
        assertEquals(0.5 * CENTER_GAIN, output.samples()[2 * frame], 1e-4);
        assertEquals(0.5 * CENTER_GAIN, output.samples()[2 * frame + 1], 1e-4);
    }

    @Test
    void monoOutputIsTheAverageOfBothChannels() {
        OutputFormat mono = new OutputFormat(44100, ChannelLayout.MONO, 0, 100);
        SynthOutput output = render(constantSynth(), SynthOptions.defaults().withOutput(mono), new NoteSpec(60, 0, 500, 127));

        assertEquals(1, output.channels());
        assertEquals(Math.round(600 * 44.1), output.frames());
        assertEquals(0.5 * CENTER_GAIN, output.samples()[4410], 1e-4);
    }

    @Test
    void masterGainScalesTheOutput() {
        OutputFormat quieter = new OutputFormat(44100, ChannelLayout.STEREO, -6.0206, 100);
        SynthOutput output = render(constantSynth(), SynthOptions.defaults().withOutput(quieter), new NoteSpec(60, 0, 500, 127));

        assertEquals(0.25 * CENTER_GAIN, output.samples()[2 * 4410], 1e-4);
    }

    @Test
    void softerVelocityIsQuieter() {
        Synthesizer synth = constantSynth();
        float loud = render(synth, SynthOptions.defaults(), new NoteSpec(60, 0, 500, 127)).samples()[2 * 4410];
        float soft = render(synth, SynthOptions.defaults(), new NoteSpec(60, 0, 500, 90)).samples()[2 * 4410];

        assertEquals(Math.pow(10, -59.82 / 200), soft / loud, 1e-3);
    }

    @Test
    void outputSampleRateResamplesTheSample() {
        OutputFormat lowRate = new OutputFormat(22050, ChannelLayout.STEREO, 0, 100);
        SynthOutput output = render(constantSynth(), SynthOptions.defaults().withOutput(lowRate), new NoteSpec(60, 0, 500, 127));

        assertEquals(22050, output.sampleRate());
        assertEquals(Math.round(600 * 22.05), output.frames());
        assertEquals(0.5 * CENTER_GAIN, output.samples()[2 * 2205], 1e-4);
    }

    @Test
    void unknownPresetIsRejected() {
        SynthOptions options = SynthOptions.defaults().withPreset(new PresetRef(0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> render(constantSynth(), options, new NoteSpec(60, 0, 100, 100)));
    }

    @Test
    void keyWithoutAZoneIsSilent() {
        SynthOutput output = render(constantSynth(), SynthOptions.defaults(), new NoteSpec(61, 0, 100, 100));

        for (float sample : output.samples()) {
            assertEquals(0.0f, sample);
        }
    }

    @Test
    void limiterKeepsLoudOverlappingNotesWithinRange() {
        Synthesizer synth = constantSynth();
        SynthOutput output = render(synth, SynthOptions.defaults(),
                new NoteSpec(60, 0, 500, 127), new NoteSpec(60, 0, 500, 127), new NoteSpec(60, 0, 500, 127));

        float peak = 0;
        for (float sample : output.samples()) {
            peak = Math.max(peak, Math.abs(sample));
        }
        assertTrue(peak > 0.8f && peak < 1.0f, "peak was " + peak);
    }

    @Test
    void lastFramesFadeToSilence() {
        OutputFormat noTail = new OutputFormat(44100, ChannelLayout.STEREO, 0, 0);
        SynthOutput output = render(constantSynth(), SynthOptions.defaults().withOutput(noTail), new NoteSpec(60, 0, 500, 127));
        float[] samples = output.samples();

        assertEquals(0.0f, samples[samples.length - 1]);
        assertEquals(0.0f, samples[samples.length - 2]);
        float fiveMsBeforeEnd = samples[samples.length - 2 * 221];
        assertTrue(fiveMsBeforeEnd > 0.3f, "fade started too early: " + fiveMsBeforeEnd);
    }

    @Test
    void requestsLongerThanTheMaximumAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> render(constantSynth(), SynthOptions.defaults(), new NoteSpec(60, 600_000, 1, 100)));
    }

    @Test
    void concurrentRendersOfTheSameRequestAreIdentical() throws Exception {
        Synthesizer synth = constantSynth();
        SynthRequest request = new SynthRequest(
                List.of(new NoteSpec(60, 0, 300, 100), new NoteSpec(60, 150, 300, 60)), SynthOptions.defaults());
        float[] expected = synth.render(request).samples();

        try (ExecutorService pool = Executors.newFixedThreadPool(4)) {
            List<Future<SynthOutput>> results = pool.invokeAll(
                    java.util.Collections.nCopies(8, () -> synth.render(request)));
            for (Future<SynthOutput> result : results) {
                assertArrayEquals(expected, result.get().samples());
            }
        }
    }
}
```

`src/test/java/vn/ktt/music/infrastructure/synth/GrandPianoSmokeTest.java`. It runs only where the real SoundFont exists, and skips cleanly elsewhere:

```java
package vn.ktt.music.infrastructure.synth;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import vn.ktt.music.infrastructure.synth.api.NoteSpec;
import vn.ktt.music.infrastructure.synth.api.SynthOptions;
import vn.ktt.music.infrastructure.synth.api.SynthOutput;
import vn.ktt.music.infrastructure.synth.api.SynthRequest;
import vn.ktt.music.infrastructure.synth.soundfont.SoundFontLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Runs only where the real (gitignored) SoundFont is present. */
class GrandPianoSmokeTest {

    private static final Path GRAND_PIANO = Path.of("src/main/resources/soundfonts/grand_piano.sf2");

    private static Synthesizer synth;

    @BeforeAll
    static void loadSoundFont() {
        assumeTrue(Files.exists(GRAND_PIANO), "grand_piano.sf2 not present");
        synth = new Synthesizer(SoundFontLoader.load(GRAND_PIANO));
    }

    private static SynthOutput middleC(int velocity) {
        return synth.render(new SynthRequest(List.of(new NoteSpec(60, 63, 376, velocity)), SynthOptions.defaults()));
    }

    private static double rms(float[] samples) {
        double sum = 0;
        for (float sample : samples) {
            sum += sample * sample;
        }
        return Math.sqrt(sum / samples.length);
    }

    @Test
    void rendersAudibleStereoWithinRange() {
        SynthOutput output = middleC(90);

        assertEquals(2, output.channels());
        assertEquals(Math.round((63 + 376 + 1000) * 44.1), output.frames());
        float peak = 0;
        for (float sample : output.samples()) {
            peak = Math.max(peak, Math.abs(sample));
        }
        assertTrue(peak > 0.01f, "silent output, peak " + peak);
        assertTrue(peak < 1.0f, "clipped output, peak " + peak);
    }

    @Test
    void softNotesAreQuieterThanLoudNotes() {
        assertTrue(rms(middleC(30).samples()) < rms(middleC(120).samples()));
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn test -Dtest='MixerTest,SynthesizerTest,GrandPianoSmokeTest'`
Expected: `COMPILATION ERROR`, with `cannot find symbol` for `Mixer` and `Synthesizer`.

- [ ] **Step 3: Write the implementation**

`src/main/java/vn/ktt/music/infrastructure/synth/engine/Mixer.java`:

```java
package vn.ktt.music.infrastructure.synth.engine;

import vn.ktt.music.infrastructure.synth.api.ChannelLayout;
import vn.ktt.music.infrastructure.synth.api.NoteSpec;
import vn.ktt.music.infrastructure.synth.api.OutputFormat;
import vn.ktt.music.infrastructure.synth.api.PresetRef;
import vn.ktt.music.infrastructure.synth.api.SynthOutput;
import vn.ktt.music.infrastructure.synth.api.SynthRequest;
import vn.ktt.music.infrastructure.synth.soundfont.Preset;
import vn.ktt.music.infrastructure.synth.soundfont.SoundFont;

/**
 * Renders a request: every note's voices are mixed into stereo buffers whose length is the last
 * release plus the tail; then master gain, a soft limiter and a short fade-out are applied.
 */
public final class Mixer {

    /** Longest audio one request may produce, so a bad onset cannot allocate gigabytes. */
    public static final long MAX_DURATION_MS = 600_000;
    static final long FADE_OUT_MS = 5;
    private static final double LIMITER_THRESHOLD = 0.8;

    private final SoundFont soundFont;

    public Mixer(SoundFont soundFont) {
        this.soundFont = soundFont;
    }

    public SynthOutput render(SynthRequest request) {
        PresetRef presetRef = request.options().preset();
        Preset preset = soundFont.findPreset(presetRef.bank(), presetRef.program())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No preset with bank " + presetRef.bank() + " and program " + presetRef.program()));
        OutputFormat output = request.options().output();
        int sampleRate = output.sampleRate();

        long lastReleaseMs = request.notes().stream().mapToLong(NoteSpec::releaseMs).max().orElseThrow();
        long durationMs = lastReleaseMs + output.tailMs();
        if (durationMs > MAX_DURATION_MS) {
            throw new IllegalArgumentException("Rendered audio would last " + durationMs
                    + " ms; the maximum is " + MAX_DURATION_MS + " ms");
        }
        int frames = Units.msToFrames(durationMs, sampleRate);
        float[] left = new float[frames];
        float[] right = new float[frames];

        for (NoteSpec note : request.notes()) {
            int startFrame = Units.msToFrames(note.onsetMs(), sampleRate);
            int releaseFrame = Units.msToFrames(note.releaseMs(), sampleRate);
            for (VoiceSpec voice : ZoneResolver.resolve(preset, note.midiKey(), note.velocity())) {
                new Voice(voice, soundFont.sampleData(), request.options().interpolation(), sampleRate,
                        startFrame, releaseFrame).renderInto(left, right);
            }
        }

        float[] samples = interleave(left, right, output.channels());
        finish(samples, output.channels().count(), Units.decibelsToGain(output.masterGainDb()),
                Units.msToFrames(FADE_OUT_MS, sampleRate));
        return new SynthOutput(samples, sampleRate, output.channels().count());
    }

    /** Soft limiter: unchanged up to 0.8, then a tanh knee towards 1 that never exceeds it. */
    static double softLimit(double x) {
        double magnitude = Math.abs(x);
        if (magnitude <= LIMITER_THRESHOLD) {
            return x;
        }
        double headroom = 1.0 - LIMITER_THRESHOLD;
        return Math.signum(x) * (LIMITER_THRESHOLD + headroom * Math.tanh((magnitude - LIMITER_THRESHOLD) / headroom));
    }

    private static float[] interleave(float[] left, float[] right, ChannelLayout channels) {
        if (channels == ChannelLayout.MONO) {
            float[] mono = new float[left.length];
            for (int i = 0; i < left.length; i++) {
                mono[i] = (left[i] + right[i]) / 2f;
            }
            return mono;
        }
        float[] stereo = new float[left.length * 2];
        for (int i = 0; i < left.length; i++) {
            stereo[2 * i] = left[i];
            stereo[2 * i + 1] = right[i];
        }
        return stereo;
    }

    private static void finish(float[] samples, int channels, double gain, int fadeFrames) {
        for (int i = 0; i < samples.length; i++) {
            samples[i] = (float) softLimit(samples[i] * gain);
        }
        int frames = samples.length / channels;
        int fade = Math.min(fadeFrames, frames);
        for (int k = 0; k < fade; k++) {
            double factor = (fade - 1 - k) / (double) fade;
            int frame = frames - fade + k;
            for (int channel = 0; channel < channels; channel++) {
                samples[frame * channels + channel] *= (float) factor;
            }
        }
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/synth/Synthesizer.java`:

```java
package vn.ktt.music.infrastructure.synth;

import vn.ktt.music.infrastructure.synth.api.SynthOutput;
import vn.ktt.music.infrastructure.synth.api.SynthRequest;
import vn.ktt.music.infrastructure.synth.engine.Mixer;
import vn.ktt.music.infrastructure.synth.soundfont.SoundFont;

/** Our SF2 sampler synthesizer: notes and sound options in, float PCM out. Stateless and thread-safe. */
public final class Synthesizer {

    private final Mixer mixer;

    public Synthesizer(SoundFont soundFont) {
        this.mixer = new Mixer(soundFont);
    }

    public SynthOutput render(SynthRequest request) {
        return mixer.render(request);
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `mvn test -Dtest='MixerTest,SynthesizerTest,GrandPianoSmokeTest,SynthPackageIsolationTest'`
Expected: `Tests run: 18, Failures: 0, Errors: 0` with `grand_piano.sf2` present. Without it, `GrandPianoSmokeTest` reports `Tests run: 0` and the total is 16.

For reference, on 2026-10-10 the real piano gave these levels at middle C: velocity 30 peaked at 0.02, velocity 90 at 0.23 and velocity 127 at 0.47. A 16-note P5 sweep rendered in about 40 ms.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/vn/ktt/music/infrastructure/synth/engine/Mixer.java src/main/java/vn/ktt/music/infrastructure/synth/Synthesizer.java src/test/java/vn/ktt/music/infrastructure/synth/engine/MixerTest.java src/test/java/vn/ktt/music/infrastructure/synth/SynthesizerTest.java src/test/java/vn/ktt/music/infrastructure/synth/GrandPianoSmokeTest.java
git commit -F - <<'MSG'
feat(synth): mix voices into limited stereo or mono PCM (#5)

Adds the Synthesizer facade. Renders are capped at 10 minutes and are
safe to run concurrently over the shared memory-mapped samples.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
MSG
```

---

### Task 8: Application payloads, ports and the interval note sequencer

**Files:**
- Create: `src/main/java/vn/ktt/music/application/sound/audio/SoundNote.java`, `src/main/java/vn/ktt/music/application/sound/audio/PcmAudio.java`, `src/main/java/vn/ktt/music/application/sound/audio/EncodedAudio.java`, `src/main/java/vn/ktt/music/application/sound/outbound/ISoundSynthesizerPort.java`, `src/main/java/vn/ktt/music/application/sound/outbound/IAudioEncoderPort.java`, `src/main/java/vn/ktt/music/application/sound/IntervalNoteSequencer.java`
- Test: `src/test/java/vn/ktt/music/application/sound/audio/SoundPayloadsTest.java`, `src/test/java/vn/ktt/music/application/sound/IntervalNoteSequencerTest.java`

**Interfaces:**
- Consumes these existing domain types:
  - `Pitch`: `toMidiNumber()` and `static convertFromMidiNumber(int)`
  - `Interval`: `getIntervalType().getHalfSteps()`, `upwardPitch(Pitch)`, and `toString()`, which gives the notation, e.g. `M3`
  - `Interval.Texture`
  - `Instrument`: `getLowestPitch()` and `getHighestPitch()`
  - `MusicalEntityFactory` and `Instrument.reconstruct` in the tests
- Produces:
  - `record SoundNote(Pitch pitch, long onsetMs, long durationMs, int velocity)` in `vn.ktt.music.application.sound.audio`
  - `record PcmAudio(float[] samples, int sampleRate, int channels)` and `record EncodedAudio(byte[] data, String mimeType, String extension)`
  - `interface ISoundSynthesizerPort { PcmAudio synthesize(List<SoundNote> notes, InstrumentType instrument); }`
  - `interface IAudioEncoderPort { EncodedAudio encode(PcmAudio pcm); }`
  - `@Component class IntervalNoteSequencer`, with `List<SoundNote> single(Pitch lower, Interval, Interval.Texture)` and `List<SoundNote> range(Interval, Interval.Texture, boolean descendingSweep, Instrument)`

Nothing uses these yet. The old `ISoundGeneratorPort` pipeline keeps serving requests until Task 10.

- [ ] **Step 1: Write the failing tests**

`src/test/java/vn/ktt/music/application/sound/audio/SoundPayloadsTest.java`:

```java
package vn.ktt.music.application.sound.audio;

import org.junit.jupiter.api.Test;
import vn.ktt.music.domain.atom.Pitch;
import vn.ktt.music.domain.factory.MusicalEntityFactory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SoundPayloadsTest {

    private static final Pitch C4 = new MusicalEntityFactory().getPitch("C4");

    @Test
    void soundNoteValidatesItsFields() {
        assertDoesNotThrow(() -> new SoundNote(C4, 0, 1, 127));
        assertThrows(IllegalArgumentException.class, () -> new SoundNote(null, 0, 1, 90));
        assertThrows(IllegalArgumentException.class, () -> new SoundNote(C4, -1, 1, 90));
        assertThrows(IllegalArgumentException.class, () -> new SoundNote(C4, 0, 0, 90));
        assertThrows(IllegalArgumentException.class, () -> new SoundNote(C4, 0, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new SoundNote(C4, 0, 1, 128));
    }

    @Test
    void pcmAudioValidatesItsShape() {
        assertDoesNotThrow(() -> new PcmAudio(new float[4], 44100, 2));
        assertThrows(IllegalArgumentException.class, () -> new PcmAudio(null, 44100, 1));
        assertThrows(IllegalArgumentException.class, () -> new PcmAudio(new float[3], 44100, 2));
        assertThrows(IllegalArgumentException.class, () -> new PcmAudio(new float[2], 44100, 3));
        assertThrows(IllegalArgumentException.class, () -> new PcmAudio(new float[2], 0, 1));
    }
}
```

`src/test/java/vn/ktt/music/application/sound/IntervalNoteSequencerTest.java`. The golden test pins today's sweep: a P5 on A0–C8 has 8 bases (MIDI 21–28) and a note every 209 ms:

```java
package vn.ktt.music.application.sound;

import org.junit.jupiter.api.Test;
import vn.ktt.music.application.sound.audio.SoundNote;
import vn.ktt.music.domain.composition.Interval;
import vn.ktt.music.domain.factory.MusicalEntityFactory;
import vn.ktt.music.domain.instrument.Instrument;
import vn.ktt.music.domain.instrument.InstrumentType;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IntervalNoteSequencerTest {

    private final MusicalEntityFactory factory = new MusicalEntityFactory();
    private final IntervalNoteSequencer sequencer = new IntervalNoteSequencer();
    private final Instrument piano = Instrument.reconstruct(factory, InstrumentType.PIANO, "A0", "C8");

    private record Timed(int key, long onsetMs, long durationMs) {
    }

    private static List<Timed> timed(List<SoundNote> notes) {
        return notes.stream()
                .map(note -> new Timed(note.pitch().toMidiNumber(), note.onsetMs(), note.durationMs()))
                .toList();
    }

    @Test
    void singleAscendingPlaysTheLowerNoteFirst() {
        List<SoundNote> notes = sequencer.single(factory.getPitch("C4"), new Interval("M3"), Interval.Texture.ASCENDING);

        assertEquals(List.of(new Timed(60, 63, 188), new Timed(64, 272, 188)), timed(notes));
        assertEquals(90, notes.getFirst().velocity());
    }

    @Test
    void singleDescendingPlaysTheUpperNoteFirst() {
        List<SoundNote> notes = sequencer.single(factory.getPitch("C4"), new Interval("M3"), Interval.Texture.DESCENDING);

        assertEquals(List.of(new Timed(64, 63, 188), new Timed(60, 272, 188)), timed(notes));
    }

    @Test
    void singleStackedPlaysBothNotesTogetherForTwiceAsLong() {
        List<SoundNote> notes = sequencer.single(factory.getPitch("C4"), new Interval("P5"), Interval.Texture.STACKED);

        assertEquals(List.of(new Timed(60, 63, 376), new Timed(67, 63, 376)), timed(notes));
    }

    @Test
    void ascendingP5RangeMatchesTheGoldenTimings() {
        List<SoundNote> notes = sequencer.range(new Interval("P5"), Interval.Texture.ASCENDING, false, piano);

        // bases A0 (21) to E1 (28): 8 bases, 16 notes, one every 209 ms
        assertEquals(16, notes.size());
        assertEquals(IntStream.range(0, 16).mapToObj(k -> 63L + k * 209L).toList(),
                notes.stream().map(SoundNote::onsetMs).toList());
        assertEquals(List.of(21, 28, 22, 29), notes.stream().limit(4).map(n -> n.pitch().toMidiNumber()).toList());
        assertEquals(35, notes.getLast().pitch().toMidiNumber());
    }

    @Test
    void descendingSweepStartsFromTheHighestBase() {
        List<SoundNote> notes = sequencer.range(new Interval("M3"), Interval.Texture.ASCENDING, true, piano);

        assertEquals(List.of(25, 29, 24, 28), notes.stream().limit(4).map(n -> n.pitch().toMidiNumber()).toList());
        assertEquals(List.of(21, 25), notes.stream().skip(8).map(n -> n.pitch().toMidiNumber()).toList());
    }

    @Test
    void stackedRangeAdvancesByTheStackedStep() {
        List<SoundNote> notes = sequencer.range(new Interval("m2"), Interval.Texture.STACKED, false, piano);

        assertEquals(List.of(new Timed(21, 63, 376), new Timed(22, 63, 376), new Timed(22, 460, 376), new Timed(23, 460, 376)),
                timed(notes));
    }

    @Test
    void rangeRejectsAnIntervalWiderThanTheInstrument() {
        Instrument narrow = Instrument.reconstruct(factory, InstrumentType.PIANO, "C4", "E4");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> sequencer.range(new Interval("P5"), Interval.Texture.ASCENDING, false, narrow));
        assertEquals("Interval P5 does not fit instrument range", error.getMessage());
    }

    @Test
    void rangeNearTheTopOfTheInstrumentIsLimitedByTheHighestPitch() {
        Instrument shortRange = Instrument.reconstruct(factory, InstrumentType.PIANO, "C4", "A4");

        List<SoundNote> notes = sequencer.range(new Interval("M3"), Interval.Texture.STACKED, false, shortRange);

        // bases C4 (60) up to min(60 + 4, 69 - 4) = 64
        assertEquals(List.of(60, 64, 61, 65, 62, 66, 63, 67, 64, 68),
                notes.stream().map(n -> n.pitch().toMidiNumber()).toList());
    }

    @Test
    void unisonStackedGivesTwoIdenticalNotes() {
        List<SoundNote> notes = sequencer.single(factory.getPitch("C4"), new Interval("P0"), Interval.Texture.STACKED);

        assertEquals(List.of(new Timed(60, 63, 376), new Timed(60, 63, 376)), timed(notes));
    }

    @Test
    void unisonRangeHasASingleBase() {
        List<SoundNote> notes = sequencer.range(new Interval("P0"), Interval.Texture.ASCENDING, false, piano);

        assertEquals(List.of(new Timed(21, 63, 188), new Timed(21, 272, 188)), timed(notes));
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn test -Dtest='SoundPayloadsTest,IntervalNoteSequencerTest'`
Expected: `COMPILATION ERROR`, with `cannot find symbol` for `SoundNote`, `PcmAudio` and `IntervalNoteSequencer`.

- [ ] **Step 3: Write the implementation**

`src/main/java/vn/ktt/music/application/sound/audio/SoundNote.java`:

```java
package vn.ktt.music.application.sound.audio;

import vn.ktt.music.domain.atom.Pitch;

public record SoundNote(Pitch pitch, long onsetMs, long durationMs, int velocity) {

    public SoundNote {
        if (pitch == null) {
            throw new IllegalArgumentException("pitch must not be null");
        }
        if (onsetMs < 0) {
            throw new IllegalArgumentException("onsetMs must be >= 0: " + onsetMs);
        }
        if (durationMs <= 0) {
            throw new IllegalArgumentException("durationMs must be > 0: " + durationMs);
        }
        if (velocity < 1 || velocity > 127) {
            throw new IllegalArgumentException("velocity must be 1-127: " + velocity);
        }
    }
}
```

`src/main/java/vn/ktt/music/application/sound/audio/PcmAudio.java`:

```java
package vn.ktt.music.application.sound.audio;

/** Interleaved float PCM in -1..1. The array is not copied. */
public record PcmAudio(float[] samples, int sampleRate, int channels) {

    public PcmAudio {
        if (samples == null) {
            throw new IllegalArgumentException("samples must not be null");
        }
        if (channels < 1 || channels > 2) {
            throw new IllegalArgumentException("channels must be 1 or 2: " + channels);
        }
        if (samples.length % channels != 0) {
            throw new IllegalArgumentException("samples length must be a multiple of channels");
        }
        if (sampleRate <= 0) {
            throw new IllegalArgumentException("sampleRate must be > 0: " + sampleRate);
        }
    }
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
import vn.ktt.music.application.sound.audio.SoundNote;
import vn.ktt.music.domain.instrument.InstrumentType;

import java.util.List;

public interface ISoundSynthesizerPort {
    PcmAudio synthesize(List<SoundNote> notes, InstrumentType instrument);
}
```

`src/main/java/vn/ktt/music/application/sound/outbound/IAudioEncoderPort.java`:

```java
package vn.ktt.music.application.sound.outbound;

import vn.ktt.music.application.sound.audio.EncodedAudio;
import vn.ktt.music.application.sound.audio.PcmAudio;

public interface IAudioEncoderPort {
    EncodedAudio encode(PcmAudio pcm);
}
```

`src/main/java/vn/ktt/music/application/sound/IntervalNoteSequencer.java`. Its timings are `MidiSequenceBuilder`'s ticks (1 tick = 1.0417 ms) rounded to whole milliseconds. The old clamp to MIDI 108 becomes the instrument's highest pitch:

```java
package vn.ktt.music.application.sound;

import org.springframework.stereotype.Component;
import vn.ktt.music.application.sound.audio.SoundNote;
import vn.ktt.music.domain.atom.Pitch;
import vn.ktt.music.domain.composition.Interval;
import vn.ktt.music.domain.instrument.Instrument;

import java.util.ArrayList;
import java.util.List;

/** Lays out interval exercises as timed notes. Timings are the old MIDI tick values rounded to whole milliseconds. */
@Component
public class IntervalNoteSequencer {

    static final long PREROLL_MS = 63;
    static final long NOTE_MS = 188;
    static final long STACKED_NOTE_MS = 376;
    static final long GAP_MS = 21;
    static final int VELOCITY = 90;

    public List<SoundNote> single(Pitch lower, Interval interval, Interval.Texture texture) {
        List<SoundNote> notes = new ArrayList<>();
        schedule(notes, lower, interval, texture, PREROLL_MS);
        return List.copyOf(notes);
    }

    /** Sweeps the interval over every base from the instrument's lowest pitch up to one interval above it. */
    public List<SoundNote> range(Interval interval, Interval.Texture texture, boolean descendingSweep, Instrument instrument) {
        int halfSteps = interval.getIntervalType().getHalfSteps();
        int lowest = instrument.getLowestPitch().toMidiNumber();
        int highestBase = Math.min(lowest + halfSteps, instrument.getHighestPitch().toMidiNumber() - halfSteps);
        if (highestBase < lowest) {
            throw new IllegalArgumentException("Interval " + interval + " does not fit instrument range");
        }
        List<SoundNote> notes = new ArrayList<>();
        long onset = PREROLL_MS;
        for (int step = 0; step <= highestBase - lowest; step++) {
            int base = descendingSweep ? highestBase - step : lowest + step;
            onset = schedule(notes, Pitch.convertFromMidiNumber(base), interval, texture, onset);
        }
        return List.copyOf(notes);
    }

    /** Adds one interval starting at {@code onset} and returns the onset of whatever comes next. */
    private long schedule(List<SoundNote> notes, Pitch lower, Interval interval, Interval.Texture texture, long onset) {
        Pitch upper = interval.upwardPitch(lower);
        long melodicStep = NOTE_MS + GAP_MS;
        return switch (texture) {
            case STACKED -> {
                notes.add(new SoundNote(lower, onset, STACKED_NOTE_MS, VELOCITY));
                notes.add(new SoundNote(upper, onset, STACKED_NOTE_MS, VELOCITY));
                yield onset + STACKED_NOTE_MS + GAP_MS;
            }
            case ASCENDING -> {
                notes.add(new SoundNote(lower, onset, NOTE_MS, VELOCITY));
                notes.add(new SoundNote(upper, onset + melodicStep, NOTE_MS, VELOCITY));
                yield onset + 2 * melodicStep;
            }
            case DESCENDING -> {
                notes.add(new SoundNote(upper, onset, NOTE_MS, VELOCITY));
                notes.add(new SoundNote(lower, onset + melodicStep, NOTE_MS, VELOCITY));
                yield onset + 2 * melodicStep;
            }
        };
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `mvn test -Dtest='SoundPayloadsTest,IntervalNoteSequencerTest'`
Expected: `Tests run: 12, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/vn/ktt/music/application/sound/audio src/main/java/vn/ktt/music/application/sound/outbound/ISoundSynthesizerPort.java src/main/java/vn/ktt/music/application/sound/outbound/IAudioEncoderPort.java src/main/java/vn/ktt/music/application/sound/IntervalNoteSequencer.java src/test/java/vn/ktt/music/application/sound
git commit -F - <<'MSG'
feat(music): add synthesizer and encoder ports and the note sequencer (#5)

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
MSG
```

---

### Task 9: WAV encoder, synthesizer adapter and Spring wiring

**Files:**
- Create: `src/main/java/vn/ktt/music/infrastructure/audio/encoder/WavAudioEncoder.java`, `src/main/java/vn/ktt/music/infrastructure/audio/SamplerSynthesizerAdapter.java`, `src/main/java/vn/ktt/music/infrastructure/config/SynthesizerConfig.java`
- Test: `src/test/java/vn/ktt/music/infrastructure/audio/encoder/WavAudioEncoderTest.java`, `src/test/java/vn/ktt/music/infrastructure/audio/SamplerSynthesizerAdapterTest.java`, `src/test/java/vn/ktt/music/infrastructure/config/SynthesizerConfigTest.java`

**Interfaces:**
- Consumes: `PcmAudio`, `EncodedAudio`, `SoundNote`, `ISoundSynthesizerPort` and `IAudioEncoderPort` (Task 8); `Synthesizer` (Task 7); `SoundFont` and `SoundFontLoader` (Task 2); the `synth.api` records (Task 1); and `TestSoundFontBuilder` (Task 2) in the tests.
- Produces:
  - `@Component WavAudioEncoder implements IAudioEncoderPort`, with MIME type `audio/wav` and extension `wav`
  - `@Component SamplerSynthesizerAdapter implements ISoundSynthesizerPort`, with constructor `(Synthesizer)`
  - `@Configuration SynthesizerConfig`, with `@Bean SoundFont soundFont(ResourceLoader, String soundfontPath)` and `@Bean Synthesizer synthesizer(SoundFont)`

After this task the app loads the SoundFont at startup. The old `WavEncoder` and Gervill path still serve requests until Task 10, and the two encoders don't clash because they are different types.

- [ ] **Step 1: Write the failing tests**

`src/test/java/vn/ktt/music/infrastructure/audio/encoder/WavAudioEncoderTest.java`. The values avoid exact .5 multiples, because `Math.round(-16383.5)` is `-16383`:

```java
package vn.ktt.music.infrastructure.audio.encoder;

import org.junit.jupiter.api.Test;
import vn.ktt.music.application.sound.audio.EncodedAudio;
import vn.ktt.music.application.sound.audio.PcmAudio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WavAudioEncoderTest {

    private final WavAudioEncoder encoder = new WavAudioEncoder();

    private static String fourCc(ByteBuffer buffer, int offset) {
        byte[] bytes = new byte[4];
        buffer.get(offset, bytes);
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    @Test
    void writesAStereoHeader() {
        EncodedAudio audio = encoder.encode(new PcmAudio(new float[6], 44100, 2));
        ByteBuffer wav = ByteBuffer.wrap(audio.data()).order(ByteOrder.LITTLE_ENDIAN);

        assertEquals(44 + 12, audio.data().length);
        assertEquals("RIFF", fourCc(wav, 0));
        assertEquals(36 + 12, wav.getInt(4));
        assertEquals("WAVE", fourCc(wav, 8));
        assertEquals("fmt ", fourCc(wav, 12));
        assertEquals(16, wav.getInt(16));
        assertEquals(1, wav.getShort(20));
        assertEquals(2, wav.getShort(22));
        assertEquals(44100, wav.getInt(24));
        assertEquals(44100 * 4, wav.getInt(28));
        assertEquals(4, wav.getShort(32));
        assertEquals(16, wav.getShort(34));
        assertEquals("data", fourCc(wav, 36));
        assertEquals(12, wav.getInt(40));
    }

    @Test
    void writesAMonoHeader() {
        ByteBuffer wav = ByteBuffer.wrap(encoder.encode(new PcmAudio(new float[3], 22050, 1)).data())
                .order(ByteOrder.LITTLE_ENDIAN);

        assertEquals(1, wav.getShort(22));
        assertEquals(22050, wav.getInt(24));
        assertEquals(22050 * 2, wav.getInt(28));
        assertEquals(2, wav.getShort(32));
        assertEquals(6, wav.getInt(40));
    }

    @Test
    void roundsAndClampsSamples() {
        float[] samples = {0f, 0.25f, -0.25f, 1f, -1f, 2f, -2f};
        ByteBuffer wav = ByteBuffer.wrap(encoder.encode(new PcmAudio(samples, 44100, 1)).data())
                .order(ByteOrder.LITTLE_ENDIAN);

        assertEquals(0, wav.getShort(44));
        assertEquals(8192, wav.getShort(46));
        assertEquals(-8192, wav.getShort(48));
        assertEquals(32767, wav.getShort(50));
        assertEquals(-32767, wav.getShort(52));
        assertEquals(32767, wav.getShort(54));
        assertEquals(-32767, wav.getShort(56));
    }

    @Test
    void reportsMimeTypeAndExtension() {
        EncodedAudio audio = encoder.encode(new PcmAudio(new float[0], 44100, 1));

        assertEquals("audio/wav", audio.mimeType());
        assertEquals("wav", audio.extension());
        assertEquals(44, audio.data().length);
    }
}
```

`src/test/java/vn/ktt/music/infrastructure/audio/SamplerSynthesizerAdapterTest.java`. The fixture only sounds on MIDI 60, so silence on D4 proves the pitch → key mapping:

```java
package vn.ktt.music.infrastructure.audio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.ktt.music.application.sound.audio.PcmAudio;
import vn.ktt.music.application.sound.audio.SoundNote;
import vn.ktt.music.domain.factory.MusicalEntityFactory;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.infrastructure.synth.Synthesizer;
import vn.ktt.music.infrastructure.synth.TestSoundFontBuilder;
import vn.ktt.music.infrastructure.synth.soundfont.GeneratorType;
import vn.ktt.music.infrastructure.synth.soundfont.SoundFontLoader;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vn.ktt.music.infrastructure.synth.TestSoundFontBuilder.gen;
import static vn.ktt.music.infrastructure.synth.TestSoundFontBuilder.range;

class SamplerSynthesizerAdapterTest {

    private final MusicalEntityFactory factory = new MusicalEntityFactory();

    @TempDir
    Path tempDir;

    /** Fixture preset (0, 0) with a sound on C4 (MIDI 60) only. */
    private SamplerSynthesizerAdapter adapter() {
        Path file = TestSoundFontBuilder.singleZone(TestSoundFontBuilder.constant(44100, 16384),
                gen(GeneratorType.KEY_RANGE, range(60, 60)),
                gen(GeneratorType.SAMPLE_MODES, 1)).writeTo(tempDir);
        return new SamplerSynthesizerAdapter(new Synthesizer(SoundFontLoader.load(file)));
    }

    private static float peak(PcmAudio pcm) {
        float peak = 0;
        for (float sample : pcm.samples()) {
            peak = Math.max(peak, Math.abs(sample));
        }
        return peak;
    }

    @Test
    void rendersPianoNotesWithTheDefaultOptions() {
        PcmAudio pcm = adapter().synthesize(
                List.of(new SoundNote(factory.getPitch("C4"), 63, 188, 90)), InstrumentType.PIANO);

        assertEquals(44100, pcm.sampleRate());
        assertEquals(2, pcm.channels());
        assertEquals(Math.round((63 + 188 + 1000) * 44.1), pcm.samples().length / 2);
        assertTrue(peak(pcm) > 0.1f);
    }

    @Test
    void mapsPitchesToMidiKeys() {
        PcmAudio silent = adapter().synthesize(
                List.of(new SoundNote(factory.getPitch("D4"), 63, 188, 90)), InstrumentType.PIANO);

        assertEquals(0.0f, peak(silent));
    }

    @Test
    void rejectsAnUnmappedInstrument() {
        List<SoundNote> notes = List.of(new SoundNote(factory.getPitch("C4"), 0, 100, 90));

        assertThrows(IllegalArgumentException.class, () -> adapter().synthesize(notes, null));
    }
}
```

`src/test/java/vn/ktt/music/infrastructure/config/SynthesizerConfigTest.java`:

```java
package vn.ktt.music.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import vn.ktt.music.infrastructure.synth.TestSoundFontBuilder;
import vn.ktt.music.infrastructure.synth.soundfont.SoundFont;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SynthesizerConfigTest {

    private final SynthesizerConfig config = new SynthesizerConfig();

    @TempDir
    Path tempDir;

    @Test
    void loadsASoundFontFromAFileLocation() {
        Path file = TestSoundFontBuilder.singleZone(TestSoundFontBuilder.constant(10, 1)).writeTo(tempDir);

        SoundFont soundFont = config.soundFont(new DefaultResourceLoader(), file.toUri().toString());

        assertTrue(soundFont.findPreset(0, 0).isPresent());
    }

    @Test
    void failsStartupWhenTheSoundFontIsMissing() {
        String missing = tempDir.resolve("missing.sf2").toUri().toString();

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> config.soundFont(new DefaultResourceLoader(), missing));
        assertTrue(error.getMessage().contains("missing.sf2"));
    }

    @Test
    void copiesASoundFontThatIsNotAPlainFile() {
        // inside a packaged jar the classpath resource is not a file, so it is copied to a temp file first
        byte[] bytes = TestSoundFontBuilder.singleZone(TestSoundFontBuilder.constant(10, 1)).build();
        DefaultResourceLoader loader = new DefaultResourceLoader();
        loader.addProtocolResolver((location, resourceLoader) ->
                location.startsWith("memory:") ? new ByteArrayResource(bytes) : null);

        SoundFont soundFont = config.soundFont(loader, "memory:grand_piano.sf2");

        assertTrue(soundFont.findPreset(0, 0).isPresent());
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn test -Dtest='WavAudioEncoderTest,SamplerSynthesizerAdapterTest,SynthesizerConfigTest'`
Expected: `COMPILATION ERROR`, with `cannot find symbol` for `WavAudioEncoder`, `SamplerSynthesizerAdapter` and `SynthesizerConfig`.

- [ ] **Step 3: Write the implementation**

`src/main/java/vn/ktt/music/infrastructure/audio/encoder/WavAudioEncoder.java`. The header is written by hand, so there's no `javax.sound`:

```java
package vn.ktt.music.infrastructure.audio.encoder;

import org.springframework.stereotype.Component;
import vn.ktt.music.application.sound.audio.EncodedAudio;
import vn.ktt.music.application.sound.audio.PcmAudio;
import vn.ktt.music.application.sound.outbound.IAudioEncoderPort;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/** Encodes PCM as a 16-bit little-endian RIFF/WAVE file with a canonical 44-byte header. */
@Component
public class WavAudioEncoder implements IAudioEncoderPort {

    static final String MIME_TYPE = "audio/wav";
    static final String EXTENSION = "wav";
    private static final int HEADER_BYTES = 44;
    private static final int FMT_CHUNK_BYTES = 16;
    private static final short PCM_FORMAT = 1;
    private static final short BITS_PER_SAMPLE = 16;
    private static final int BYTES_PER_SAMPLE = BITS_PER_SAMPLE / 8;

    @Override
    public EncodedAudio encode(PcmAudio pcm) {
        int channels = pcm.channels();
        int dataBytes = Math.multiplyExact(pcm.samples().length, BYTES_PER_SAMPLE);
        ByteBuffer out = ByteBuffer.allocate(HEADER_BYTES + dataBytes).order(ByteOrder.LITTLE_ENDIAN);
        out.put("RIFF".getBytes(StandardCharsets.US_ASCII))
                .putInt(HEADER_BYTES - 8 + dataBytes)
                .put("WAVE".getBytes(StandardCharsets.US_ASCII))
                .put("fmt ".getBytes(StandardCharsets.US_ASCII))
                .putInt(FMT_CHUNK_BYTES)
                .putShort(PCM_FORMAT)
                .putShort((short) channels)
                .putInt(pcm.sampleRate())
                .putInt(pcm.sampleRate() * channels * BYTES_PER_SAMPLE)
                .putShort((short) (channels * BYTES_PER_SAMPLE))
                .putShort(BITS_PER_SAMPLE)
                .put("data".getBytes(StandardCharsets.US_ASCII))
                .putInt(dataBytes);
        for (float sample : pcm.samples()) {
            out.putShort((short) Math.round(Math.clamp(sample, -1f, 1f) * Short.MAX_VALUE));
        }
        return new EncodedAudio(out.array(), MIME_TYPE, EXTENSION);
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/audio/SamplerSynthesizerAdapter.java`:

```java
package vn.ktt.music.infrastructure.audio;

import org.springframework.stereotype.Component;
import vn.ktt.music.application.sound.audio.PcmAudio;
import vn.ktt.music.application.sound.audio.SoundNote;
import vn.ktt.music.application.sound.outbound.ISoundSynthesizerPort;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.infrastructure.synth.Synthesizer;
import vn.ktt.music.infrastructure.synth.api.NoteSpec;
import vn.ktt.music.infrastructure.synth.api.PresetRef;
import vn.ktt.music.infrastructure.synth.api.SynthOptions;
import vn.ktt.music.infrastructure.synth.api.SynthOutput;
import vn.ktt.music.infrastructure.synth.api.SynthRequest;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Plays application notes on our SF2 synthesizer with built-in default sound options. */
@Component
public class SamplerSynthesizerAdapter implements ISoundSynthesizerPort {

    private static final Map<InstrumentType, PresetRef> PRESETS = new EnumMap<>(Map.of(
            InstrumentType.PIANO, new PresetRef(0, 0)));

    private final Synthesizer synthesizer;

    public SamplerSynthesizerAdapter(Synthesizer synthesizer) {
        this.synthesizer = synthesizer;
    }

    @Override
    public PcmAudio synthesize(List<SoundNote> notes, InstrumentType instrument) {
        PresetRef preset = instrument == null ? null : PRESETS.get(instrument);
        if (preset == null) {
            throw new IllegalArgumentException("No SoundFont preset is mapped for instrument " + instrument);
        }
        List<NoteSpec> specs = notes.stream()
                .map(note -> new NoteSpec(note.pitch().toMidiNumber(), note.onsetMs(), note.durationMs(), note.velocity()))
                .toList();
        SynthOutput output = synthesizer.render(new SynthRequest(specs, SynthOptions.defaults().withPreset(preset)));
        return new PcmAudio(output.samples(), output.sampleRate(), output.channels());
    }
}
```

`src/main/java/vn/ktt/music/infrastructure/config/SynthesizerConfig.java`. `soundfont.path` in `application.properties` already points at `classpath:soundfonts/grand_piano.sf2`:

```java
package vn.ktt.music.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import vn.ktt.music.infrastructure.synth.Synthesizer;
import vn.ktt.music.infrastructure.synth.soundfont.SoundFont;
import vn.ktt.music.infrastructure.synth.soundfont.SoundFontLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Configuration
public class SynthesizerConfig {

    @Bean
    public SoundFont soundFont(ResourceLoader resourceLoader,
                               @Value("${soundfont.path:classpath:soundfonts/grand_piano.sf2}") String soundfontPath) {
        Resource resource = resourceLoader.getResource(soundfontPath);
        if (!resource.exists()) {
            throw new IllegalStateException("SoundFont not found at " + soundfontPath
                    + ". Put grand_piano.sf2 in src/main/resources/soundfonts or set soundfont.path.");
        }
        return SoundFontLoader.load(toFile(resource, soundfontPath));
    }

    @Bean
    public Synthesizer synthesizer(SoundFont soundFont) {
        return new Synthesizer(soundFont);
    }

    /** The loader memory-maps a file, so a resource inside a jar is copied out to a temp file once. */
    private static Path toFile(Resource resource, String location) {
        try {
            if (resource.isFile()) {
                return resource.getFile().toPath();
            }
            Path copy = Files.createTempFile("soundfont-", ".sf2");
            copy.toFile().deleteOnExit();
            try (InputStream in = resource.getInputStream()) {
                Files.copy(in, copy, StandardCopyOption.REPLACE_EXISTING);
            }
            return copy;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read SoundFont at " + location, e);
        }
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `mvn test -Dtest='WavAudioEncoderTest,SamplerSynthesizerAdapterTest,SynthesizerConfigTest'`
Expected: `Tests run: 10, Failures: 0, Errors: 0`

Then run `mvn test`. Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/vn/ktt/music/infrastructure/audio/encoder/WavAudioEncoder.java src/main/java/vn/ktt/music/infrastructure/audio/SamplerSynthesizerAdapter.java src/main/java/vn/ktt/music/infrastructure/config/SynthesizerConfig.java src/test/java/vn/ktt/music/infrastructure
git commit -F - <<'MSG'
feat(music): wire the SF2 synthesizer and a javax-free WAV encoder (#5)

The SoundFont is now loaded once at startup instead of on every request.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
MSG
```

---

### Task 10: Switch the interval service to the synthesizer and remove the MIDI pipeline

**Files:**
- Modify (full replacement): `src/main/java/vn/ktt/music/application/sound/dto/AudioContent.java`, `src/main/java/vn/ktt/music/application/sound/IntervalGeneratorService.java`
- Modify: `src/main/java/vn/ktt/music/infrastructure/controller/IntervalsController.java` (the `return` block), `src/main/java/vn/ktt/music/infrastructure/controller/IntervalRangeController.java` (the `return` block), `pom.xml` (`spring-boot-maven-plugin`), `CLAUDE.md`
- Delete:
  - `src/main/java/vn/ktt/music/infrastructure/audio/MidiSoundGenerator.java`, `src/main/java/vn/ktt/music/infrastructure/audio/midi/MidiSequenceBuilder.java`
  - `src/main/java/vn/ktt/music/infrastructure/audio/renderer/IMidiRenderer.java`, `HarmonicMidiRenderer.java`, `Sf2BasedMidiRenderer.java`, `PcmSamples.java`
  - `src/main/java/vn/ktt/music/infrastructure/audio/encoder/WavEncoder.java`
  - `src/main/java/vn/ktt/music/application/sound/outbound/ISoundGeneratorPort.java`, `src/main/java/vn/ktt/music/application/sound/dto/IntervalRangeParameters.java`
- Test: `src/test/java/vn/ktt/music/application/sound/IntervalGeneratorServiceTest.java`

**Interfaces:**
- Consumes: `IntervalNoteSequencer`, `ISoundSynthesizerPort`, `IAudioEncoderPort`, `SoundNote`, `PcmAudio` and `EncodedAudio` (Task 8), plus the existing `IMusicalOperation`, `IInstrumentConfigurationPort` and `IIntervalGeneratorPort`.
- Produces:
  - `record AudioContent(byte[] data, String mimeType, String fileName)`
  - `IntervalGeneratorService` keeps implementing `IIntervalGeneratorPort` unchanged, with constructor `(IntervalNoteSequencer, ISoundSynthesizerPort, IAudioEncoderPort, IMusicalOperation, IInstrumentConfigurationPort)`

- [ ] **Step 1: Write the failing test**

`src/test/java/vn/ktt/music/application/sound/IntervalGeneratorServiceTest.java`. It uses hand-written fakes, a `MusicalOperation` that always picks the lowest start, and a lambda for the single-method `IInstrumentConfigurationPort`:

```java
package vn.ktt.music.application.sound;

import org.junit.jupiter.api.Test;
import vn.ktt.music.application.sound.audio.EncodedAudio;
import vn.ktt.music.application.sound.audio.PcmAudio;
import vn.ktt.music.application.sound.audio.SoundNote;
import vn.ktt.music.application.sound.dto.AudioContent;
import vn.ktt.music.application.sound.outbound.IAudioEncoderPort;
import vn.ktt.music.application.sound.outbound.ISoundSynthesizerPort;
import vn.ktt.music.domain.atom.Pitch;
import vn.ktt.music.domain.composition.Interval;
import vn.ktt.music.domain.factory.MusicalEntityFactory;
import vn.ktt.music.domain.instrument.Instrument;
import vn.ktt.music.domain.instrument.InstrumentType;
import vn.ktt.music.domain.service.MusicalOperation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class IntervalGeneratorServiceTest {

    private static final byte[] WAV_BYTES = {1, 2, 3};
    private static final PcmAudio PCM = new PcmAudio(new float[2], 44100, 2);

    private final Instrument piano = Instrument.reconstruct(new MusicalEntityFactory(), InstrumentType.PIANO, "A0", "C8");

    private static final class RecordingSynthesizer implements ISoundSynthesizerPort {
        List<SoundNote> notes;
        InstrumentType instrument;

        @Override
        public PcmAudio synthesize(List<SoundNote> notes, InstrumentType instrument) {
            this.notes = notes;
            this.instrument = instrument;
            return PCM;
        }
    }

    private static final class RecordingEncoder implements IAudioEncoderPort {
        PcmAudio pcm;

        @Override
        public EncodedAudio encode(PcmAudio pcm) {
            this.pcm = pcm;
            return new EncodedAudio(WAV_BYTES, "audio/wav", "wav");
        }
    }

    /** Always picks the lowest allowed start pitch. */
    private static final class LowestPitchOperation extends MusicalOperation {
        @Override
        public Pitch getRandomPitch(Pitch lowerBoundPitch, Pitch upperBoundPitch) {
            return lowerBoundPitch;
        }
    }

    private final RecordingSynthesizer synthesizer = new RecordingSynthesizer();
    private final RecordingEncoder encoder = new RecordingEncoder();
    private final IntervalGeneratorService service = new IntervalGeneratorService(
            new IntervalNoteSequencer(), synthesizer, encoder, new LowestPitchOperation(), () -> piano);

    private List<Integer> keys() {
        return synthesizer.notes.stream().map(note -> note.pitch().toMidiNumber()).toList();
    }

    @Test
    void generateIntervalRendersOneIntervalFromTheChosenStart() {
        AudioContent audio = service.generateInterval(new Interval("M3"), Interval.Texture.ASCENDING);

        assertEquals(List.of(21, 25), keys());
        assertEquals(InstrumentType.PIANO, synthesizer.instrument);
        assertSame(PCM, encoder.pcm);
        assertArrayEquals(WAV_BYTES, audio.data());
        assertEquals("audio/wav", audio.mimeType());
        assertEquals("interval-M3.wav", audio.fileName());
    }

    @Test
    void upwardRangeSweepsFromTheLowestBase() {
        AudioContent audio = service.generateUpwardInterval(new Interval("M3"), Interval.Texture.ASCENDING);

        assertEquals(List.of(21, 25), keys().subList(0, 2));
        assertEquals(10, keys().size());
        assertEquals("interval-range-M3.wav", audio.fileName());
    }

    @Test
    void downwardRangeSweepsFromTheHighestBase() {
        AudioContent audio = service.generateDownwardInterval(new Interval("M3"), Interval.Texture.ASCENDING);

        assertEquals(List.of(25, 29), keys().subList(0, 2));
        assertEquals("interval-range-M3.wav", audio.fileName());
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `mvn test -Dtest=IntervalGeneratorServiceTest`
Expected: `COMPILATION ERROR`: there's no constructor taking `IntervalNoteSequencer`, and `AudioContent` has no `data()`, `mimeType()` or `fileName()`.

- [ ] **Step 3: Replace `AudioContent` and `IntervalGeneratorService`**

`src/main/java/vn/ktt/music/application/sound/dto/AudioContent.java` (the whole file):

```java
package vn.ktt.music.application.sound.dto;

public record AudioContent(byte[] data, String mimeType, String fileName) {
}
```

`src/main/java/vn/ktt/music/application/sound/IntervalGeneratorService.java` (the whole file):

```java
package vn.ktt.music.application.sound;

import org.springframework.stereotype.Service;
import vn.ktt.music.application.instrument.outbound.IInstrumentConfigurationPort;
import vn.ktt.music.application.sound.audio.EncodedAudio;
import vn.ktt.music.application.sound.audio.PcmAudio;
import vn.ktt.music.application.sound.audio.SoundNote;
import vn.ktt.music.application.sound.dto.AudioContent;
import vn.ktt.music.application.sound.inbound.IIntervalGeneratorPort;
import vn.ktt.music.application.sound.outbound.IAudioEncoderPort;
import vn.ktt.music.application.sound.outbound.ISoundSynthesizerPort;
import vn.ktt.music.domain.atom.Pitch;
import vn.ktt.music.domain.composition.Interval;
import vn.ktt.music.domain.instrument.Instrument;
import vn.ktt.music.domain.service.IMusicalOperation;

import java.util.List;

@Service
public class IntervalGeneratorService implements IIntervalGeneratorPort {

    private final IntervalNoteSequencer sequencer;
    private final ISoundSynthesizerPort synthesizer;
    private final IAudioEncoderPort encoder;
    private final IMusicalOperation musicalOperation;
    private final IInstrumentConfigurationPort instrumentConfigurationPort;

    public IntervalGeneratorService(IntervalNoteSequencer sequencer,
                                    ISoundSynthesizerPort synthesizer,
                                    IAudioEncoderPort encoder,
                                    IMusicalOperation musicalOperation,
                                    IInstrumentConfigurationPort instrumentConfigurationPort) {
        this.sequencer = sequencer;
        this.synthesizer = synthesizer;
        this.encoder = encoder;
        this.musicalOperation = musicalOperation;
        this.instrumentConfigurationPort = instrumentConfigurationPort;
    }

    @Override
    public AudioContent generateUpwardInterval(Interval interval, Interval.Texture texture) {
        return generateIntervalRange(interval, texture, false);
    }

    @Override
    public AudioContent generateDownwardInterval(Interval interval, Interval.Texture texture) {
        return generateIntervalRange(interval, texture, true);
    }

    @Override
    public AudioContent generateInterval(Interval interval, Interval.Texture texture) {
        Instrument instrument = instrumentConfigurationPort.getActiveInstrument();
        Pitch highestStart = musicalOperation.getLowerBoundPitchFromInterval(
                instrument.getHighestPitch(), interval.getIntervalType());
        Pitch start = musicalOperation.getRandomPitch(instrument.getLowestPitch(), highestStart);
        return render(sequencer.single(start, interval, texture), instrument, "interval-" + interval);
    }

    private AudioContent generateIntervalRange(Interval interval, Interval.Texture texture, boolean descendingSweep) {
        Instrument instrument = instrumentConfigurationPort.getActiveInstrument();
        return render(sequencer.range(interval, texture, descendingSweep, instrument), instrument,
                "interval-range-" + interval);
    }

    private AudioContent render(List<SoundNote> notes, Instrument instrument, String baseName) {
        PcmAudio pcm = synthesizer.synthesize(notes, instrument.getInstrumentType());
        EncodedAudio encoded = encoder.encode(pcm);
        return new AudioContent(encoded.data(), encoded.mimeType(), baseName + "." + encoded.extension());
    }
}
```

- [ ] **Step 4: Update the controllers to the record accessors and the encoder's MIME type**

In `src/main/java/vn/ktt/music/infrastructure/controller/IntervalsController.java`, replace:

```java
                .contentType(MediaType.parseMediaType("audio/wav"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + intervalAudioContent.getFileName() + "\"")
                .body(intervalAudioContent.getData());
```

with:

```java
                .contentType(MediaType.parseMediaType(intervalAudioContent.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + intervalAudioContent.fileName() + "\"")
                .body(intervalAudioContent.data());
```

In `src/main/java/vn/ktt/music/infrastructure/controller/IntervalRangeController.java`, replace:

```java
                .contentType(MediaType.parseMediaType("audio/wav"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + audio.getFileName() + "\"")
                .body(audio.getData());
```

with:

```java
                .contentType(MediaType.parseMediaType(audio.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + audio.fileName() + "\"")
                .body(audio.data());
```

- [ ] **Step 5: Delete the MIDI pipeline**

```bash
git rm src/main/java/vn/ktt/music/infrastructure/audio/MidiSoundGenerator.java \
  src/main/java/vn/ktt/music/infrastructure/audio/midi/MidiSequenceBuilder.java \
  src/main/java/vn/ktt/music/infrastructure/audio/renderer/IMidiRenderer.java \
  src/main/java/vn/ktt/music/infrastructure/audio/renderer/HarmonicMidiRenderer.java \
  src/main/java/vn/ktt/music/infrastructure/audio/renderer/Sf2BasedMidiRenderer.java \
  src/main/java/vn/ktt/music/infrastructure/audio/renderer/PcmSamples.java \
  src/main/java/vn/ktt/music/infrastructure/audio/encoder/WavEncoder.java \
  src/main/java/vn/ktt/music/application/sound/outbound/ISoundGeneratorPort.java \
  src/main/java/vn/ktt/music/application/sound/dto/IntervalRangeParameters.java
```

- [ ] **Step 6: Drop `--add-exports` from `pom.xml`**

Replace:

```xml
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <jvmArguments>--add-exports=java.desktop/com.sun.media.sound=ALL-UNNAMED</jvmArguments>
                </configuration>
```

with:

```xml
                <artifactId>spring-boot-maven-plugin</artifactId>
```

- [ ] **Step 7: Update `CLAUDE.md`**

In the Commands notes, delete this bullet:

```markdown
- The runtime JVM needs `--add-exports=java.desktop/com.sun.media.sound=ALL-UNNAMED`. `pom.xml` already sets it for `spring-boot:run`. Add it by hand to IDE run configs.
```

Replace:

```markdown
- `src/main/resources/soundfonts/grand_piano.sf2` (~266 MB, gitignored) must exist for `Sf2BasedMidiRenderer`, which is `@Primary`.
```

with:

```markdown
- `src/main/resources/soundfonts/grand_piano.sf2` (~266 MB, gitignored) must exist: `SynthesizerConfig` memory-maps it at startup, and the app does not start without it. `mvn test` doesn't need it; `GrandPianoSmokeTest` runs only when it's present.
```

In Architecture, replace:

```markdown
- `music`: pitches, intervals, and chords (`MusicalEntityFactory` parses notation like `M2`/`P5`), plus audio generation: MIDI `Sequence` → `IMidiRenderer` (SF2, or the `HarmonicMidiRenderer` oscillator) → PCM → `WavEncoder`.
```

with:

```markdown
- `music`: pitches, intervals, and chords (`MusicalEntityFactory` parses notation like `M2`/`P5`), plus audio generation: `IntervalNoteSequencer` lays out `SoundNote`s → `ISoundSynthesizerPort` → `PcmAudio` → `IAudioEncoderPort` (`WavAudioEncoder`). The synthesizer is our own SF2 sampler in `infrastructure/synth` (design: `docs/superpowers/specs/2026-10-10-sf2-sampler-synthesizer-design.md`), reached through `SamplerSynthesizerAdapter`. That package must not import Spring, `javax.sound`, Lombok or other `vn.ktt` code; `SynthPackageIsolationTest` enforces it.
```

- [ ] **Step 8: Run the whole suite and check that no production code uses `javax.sound` any more**

Run: `mvn test`
Expected: `Tests run: 172, Failures: 0, Errors: 0` and `BUILD SUCCESS` with `grand_piano.sf2` present (170 without it).

Run: `grep -rn "javax.sound\|add-exports\|getFileName()\|getData()" src/main pom.xml`
Expected: no output.

- [ ] **Step 9: Verify the running app by ear**

```bash
docker compose up -d
mvn spring-boot:run          # in a second terminal; there's no --add-exports flag any more
curl -sS -D - -o /tmp/m3.wav 'http://localhost:8080/api/intervals/M3/random?texture=ascending'
curl -sS -o /tmp/p5-up.wav 'http://localhost:8080/api/interval-range/P5?texture=stacked&direction=up'
curl -sS -o /tmp/p5-down.wav 'http://localhost:8080/api/interval-range/P5?texture=descending&direction=down'
file /tmp/m3.wav /tmp/p5-up.wav /tmp/p5-down.wav
```

Expected:
- The headers include `Content-Type: audio/wav` and `Content-Disposition: attachment; filename="interval-M3.wav"`.
- `file` reports `RIFF (little-endian) data, WAVE audio, Microsoft PCM, 16 bit, stereo 44100 Hz` for each file.
- When you play the files: no click at the start (the old WAVE-header garbage is gone), a stereo image, a natural roughly 0.8 s release after each note, and the P5 sweep moving up or down as requested.
- The app also starts from an IDE run config that has no JVM flags.

If any of these is wrong, stop and report it. Don't adjust constants to fit.

- [ ] **Step 10: Commit**

```bash
git add -A src/main/java/vn/ktt/music/application/sound src/main/java/vn/ktt/music/infrastructure src/test/java/vn/ktt/music/application/sound pom.xml CLAUDE.md
git commit -F - <<'MSG'
refactor(music): play intervals on our own SF2 synthesizer (#5)

IntervalGeneratorService now sequences notes, synthesizes them through
ISoundSynthesizerPort and encodes them through IAudioEncoderPort. The
javax.sound MIDI pipeline, Gervill, and the --add-exports flag are gone.
Output is now stereo WAV with a 1 s tail.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
MSG
```

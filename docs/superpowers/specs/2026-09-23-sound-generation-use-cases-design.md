# Sound generation use cases: design

- **Ticket:** #5 [Refactoring] Improve sound generation
- **Bounded context:** `vn.ktt.music`
- **Status:** approved design. The implementation plan is still to be written.
- **Reviews:** two ECC `architect` reviews, both on 2026-09-23. The draft design's verdict was *deviates, fixable*. This spec's verdict was *ready with fixes*. Every finding from both reviews is incorporated below.

## 1. Problem and acceptance criteria

The backend can generate audio, but:

- Sound characteristics are hard-coded. Timing and velocity are constants in `MidiSequenceBuilder`.
- The output is WAV-only. `WavEncoder`, the `.wav` file names in `IntervalGeneratorService`, and `audio/wav` in both controllers all hard-code it, so adding a format means editing existing code (an OCP violation).
- The flow is tied to MIDI. The outbound `ISoundGeneratorPort` takes interval-specific arguments, and its only implementation is a fixed chain: MIDI `Sequence`, then `IMidiRenderer`, then `WavEncoder`.

The acceptance criteria from the ticket:

1. The user can partially configure sound characteristics.
2. The sound generation flow is technology-agnostic: it doesn't depend on MIDI or any other synthesis technique.
3. The design follows SOLID strictly.

### Decisions

| Topic | Decision |
|---|---|
| Configurable characteristics | Note duration, silence between notes, loudness, instrument |
| Where settings live | Saved defaults in `musical_config`, which each request can override field by field |
| Output formats | A pluggable encoder seam. Only WAV ships in this ticket. |
| Pipeline | The domain builds a `Score` (MIDI-free). An `ISoundSynthesizerPort` turns it into `PcmAudio`, and an `IAudioEncoderPort` picked by format turns that into `EncodedAudio`. |

**Out of scope:**

- Per-user settings, since there are no users or auth yet.
- Non-WAV encoders.
- Delivering audio to eartraining over gRPC. `sound_service.proto` is unchanged.
- The `@RestControllerAdvice` work.

## 2. Behavioral use cases

**Actor:** Client (the frontend or an API consumer). There's no auth, so the same client both generates audio and configures settings. The eartraining context, calling over gRPC, is a possible future secondary actor. Nothing in this design prevents it.

**Settings resolution rule:** each field comes from the first source that has it: the request override, then the saved default, then the built-in default.

**Bounds** (the domain enforces them):

| Field | Allowed range |
|---|---|
| `noteDurationMs` | 50–4000 |
| `silenceMs` | 0–2000 |
| `loudness` | 1–100 |

### UC-1: Generate a single-interval sound

- **Input:** interval notation (e.g. `M3`), texture (`ASCENDING` / `DESCENDING` / `STACKED`), optional overrides, and an optional format (default `wav`).
- **Main flow:**
  1. Resolve the settings.
  2. Pick a random starting pitch in `[instrument.lowest, instrument.highest − halfSteps]`.
  3. Compose the score.
  4. Synthesize it.
  5. Encode it.
  6. Return the bytes, MIME type and file name `interval-<notation>.<ext>`.
- **Alternate flows:**
  - Invalid input: an unknown interval or texture, an interval wider than the instrument's range, an override out of bounds, an unknown instrument, or an unsupported format.
  - A synthesis or encoding failure is a server error.

### UC-2: Generate an interval-range sound

- **Input:** the same as UC-1, plus a direction (`UP` / `DOWN`).
- **Main flow:** the same as UC-1, except step 2 is a sweep. The lower note takes every base pitch from `instrument.lowest` to `instrument.lowest + halfSteps` inclusive. `UP` goes ascending and `DOWN` goes descending. The file name is `interval-range-<notation>.<ext>`.
  - This is the **current behavior**: `IntervalGeneratorService.java:54` together with `MusicalOperation.getUpperBoundPitchFromInterval`. It is *not* a sweep across the whole instrument, which would take about 30 s for a P5.
- **Alternate flows:** the same as UC-1, plus an unknown direction is invalid input.

### UC-3: View sound settings

- **Output:** the saved `noteDurationMs`, `silenceMs`, `loudness` and `instrument`, plus `availableInstruments` and `availableFormats`, so a UI can render its choices.

### UC-4: Update sound settings

- **Input:** a partial patch, meaning any subset of the four fields.
- **Main flow:**
  1. Load the saved settings.
  2. Apply the patch through the domain, which validates it.
  3. Verify that the instrument exists.
  4. Save.
  5. Return the result as in UC-3.
- **Alternate flows:** a value out of bounds, or an instrument with no `instruments` row, is invalid input.
- **Postcondition:** later UC-1 and UC-2 calls use the new defaults for any field they don't override.
- **Transactions:** UC-4 writes to one table only, so the missing `@Transactional` is not an atomicity risk here.

## 3. Domain layer (`vn.ktt.music.domain`)

Nothing in this layer depends on Spring, MIDI or `javax.sound`.

**`sound/valueobject/NoteEvent`**

- A record `(Pitch pitch, long onsetMs, long durationMs, int loudness)`.
- Requires `onsetMs ≥ 0`, `durationMs > 0` and `loudness` in 1–100.

**`sound/valueobject/Score`**

- A record holding an immutable `List<NoteEvent>`, stored as a defensive copy.
- Must be non-empty.

**`sound/valueobject/SoundSettings`**

- Fields: `int noteDurationMs`, `int silenceMs`, `int loudness`, `InstrumentType instrument`. The constructor validates the bounds, rejects a `null` instrument, and throws `IllegalArgumentException` for either.
- `static SoundSettings defaults()` returns `(188, 21, 71, PIANO)`.
- `SoundSettings withOverrides(Integer noteDurationMs, Integer silenceMs, Integer loudness, InstrumentType instrument)`:
  - A `null` argument keeps the current value.
  - It returns a new, validated instance.
  - It takes **domain and primitive types only**. The application patch DTO never crosses into the domain.

**`sound/valueobject/Direction`:** an enum with `UP` and `DOWN`, plus `fromString`, which ignores case and throws `IllegalArgumentException` for an unknown value.

**`instrument/InstrumentType`:** gains `fromString(String)`, which ignores case and throws `IllegalArgumentException` for an unknown or `null` value. Today the code would call `valueOf`, which throws a `NullPointerException` for `null` (a 500 that stays a 500 even after the planned advice lands) and is case-sensitive.

**`sound/repository/ISoundSettingsRepository`:** `SoundSettings load()` and `void save(SoundSettings)`.

**`instrument/repository/IInstrumentRepository`:** `Optional<Instrument> findByType(InstrumentType)` and `List<Instrument> findAll()`.

**`service/IIntervalScoreComposer` and `service/IntervalScoreComposer`:**

- Wired as a `@Bean` in `MusicalDomainServiceConfig`.
- Two methods:
  - `Score single(Interval, Interval.Texture, Pitch start, SoundSettings)`
  - `Score range(Interval, Interval.Texture, Direction, Instrument, SoundSettings)`
- It is deterministic. The use case picks the random `start` through `IMusicalOperation`, as the code does today.
- It takes over the loop in `MidiSequenceBuilder` and keeps its rules:
  - `PREROLL_MS = 63` is a constant, not a setting.
  - Melodic notes last `noteDurationMs`, and the next onset is `+ noteDurationMs + silenceMs`.
  - `STACKED` notes start together, last `2 × noteDurationMs`, and advance by `2 × noteDurationMs + silenceMs`.
  - `DESCENDING` plays the upper note first.
  - `HIGHEST_MIDI_NOTE = 108` is replaced by a check in `range`: it throws `IllegalArgumentException` when `lowest + 2 × halfSteps > highest`, comparing MIDI numbers. Today's code clamps instead, but with the A0–C8 seed neither the clamp nor the throw ever triggers.
  - `single` does no range check. UC-1 step 4 already guarantees that the upper note fits.

**Timing change (documented):** today 1 tick = 1.0417 ms, so the old timings are 187.5 ms, 375 ms, 20.83 ms and 62.5 ms. The new defaults are whole milliseconds: 188, 376, 21 and 63. Each melodic step goes from 208.3 ms to 209 ms. The drift adds up to about 17 ms by the end of a P8 sweep, which can't be heard.

## 4. Application layer (`vn.ktt.music.application`)

### Inbound ports and use cases

| UC | Inbound port | Use case | Method |
|---|---|---|---|
| UC-1 | `sound/inbound/IIntervalSoundPort` | `sound/IntervalSoundUseCase` | `AudioContent generateInterval(IntervalSoundCommand)` |
| UC-2 | (same port) | (same use case) | `AudioContent generateIntervalRange(IntervalRangeSoundCommand)` |
| UC-3 | `settings/inbound/ISoundSettingsQueryPort` | `settings/SoundSettingsQueryUseCase` | `SoundSettingsDTO getSettings()` |
| UC-4 | `settings/inbound/IUpdateSoundSettingsPort` | `settings/UpdateSoundSettingsUseCase` | `SoundSettingsDTO update(SoundSettingsPatch)` |

- UC-1 and UC-2 share one port, like the multi-method `ISessionPort`.
- UC-3 and UC-4 are split into a query side and an update side, mirroring `IExerciseRetrievalPort` / `IExerciseCreationPort`.

### DTOs (records, in `sound/dto` and `settings/dto`)

- `IntervalSoundCommand(String interval, String texture, SoundSettingsPatch overrides, String format)`
- `IntervalRangeSoundCommand(String interval, String texture, String direction, SoundSettingsPatch overrides, String format)`
- `SoundSettingsPatch(Integer noteDurationMs, Integer silenceMs, Integer loudness, String instrument)`. Every field is nullable. It serves both as the request overrides and as the UC-4 patch.
- `SoundSettingsDTO(int noteDurationMs, int silenceMs, int loudness, String instrument, List<String> availableInstruments, List<String> availableFormats)`
- `AudioContent(byte[] data, String mimeType, String fileName)` replaces the Lombok class. `fileSize` is dropped because it's just `data.length`.

The use cases take strings and do the parsing themselves, through `IMusicalEntityFactory`, `Interval.Texture.fromString`, `Direction.fromString`, `InstrumentType.fromString` and `AudioFormat.fromString`. All of these ignore case and throw `IllegalArgumentException`. That way REST and any future gRPC entry point share the same validation, and controllers stop parsing domain types.

**Parsing rules:**

- A `null` `overrides` is treated as an empty patch.
- `InstrumentType.fromString` is called only when `overrides.instrument()` is non-null.
- A `null` `format` means `WAV`. The use case resolves this, not a controller `defaultValue`.

**PATCH semantics:**

- An explicit `null` and an absent field mean the same thing: keep the current value. There is no reset-to-default.
- Unknown JSON fields are ignored, because Spring Boot's mapper does that by default. So `{"loudnes": 50}` returns 200 and changes nothing. This is accepted and documented.
- Patch fields stay boxed (`Integer`), because Jackson 3 turns on `FAIL_ON_NULL_FOR_PRIMITIVES`.

**`settings/SoundSettingsDTOAssembler`:** a single `@Component` that builds `SoundSettingsDTO` for both UC-3 and UC-4. `availableInstruments` and `availableFormats` are `name()` strings.

### Outbound ports (`sound/outbound`), replacing `ISoundGeneratorPort`

- `ISoundSynthesizerPort`: `PcmAudio synthesize(Score score, InstrumentType instrument)`. Loudness already travels on each `NoteEvent`.
- `IAudioEncoderPort`: `AudioFormat format()` and `EncodedAudio encode(PcmAudio)`.

### Payload types (`sound/audio`)

These are payloads carried by the ports. They are not domain concepts.

- `AudioFormat`: an enum with `WAV`, plus `fromString`. Adding a format means adding a constant, following the same pattern as the `*Type` constants.
- `PcmAudio(float[] samples, float sampleRate, int channels)` replaces the infrastructure type `PcmSamples`.
- `EncodedAudio(byte[] data, String mimeType, String extension)`

### Application services (`sound/`)

**`AudioEncoderRegistry`:**

- A `@Component` built from `List<IAudioEncoderPort>`.
- It throws `IllegalStateException` at startup if two encoders claim the same format.
- `IAudioEncoderPort get(AudioFormat)` throws `IllegalArgumentException` for a format with no encoder.
- `List<AudioFormat> supportedFormats()` lists the formats that have encoders.
- It doesn't reuse `vn.ktt.shared.ServiceRegistry`, for two reasons: `ServiceRegistry` keys on `key.getClass()` (`ServiceRegistry.java:18`), where every enum constant would collide, and it silently overwrites duplicates.

**`SoundRenderingService`:** `AudioContent render(Score, InstrumentType, AudioFormat, String baseName)`:

```
pcm     = synthesizer.synthesize(score, instrument)
encoded = encoderRegistry.get(format).encode(pcm)
return new AudioContent(encoded.data(), encoded.mimeType(), baseName + "." + encoded.extension())
```

### Use case flows

**`IntervalSoundUseCase.generateInterval`**

1. Parse the interval, texture and format.
2. Resolve the settings: `settingsRepository.load().withOverrides(o.noteDurationMs(), o.silenceMs(), o.loudness(), o.instrument() == null ? null : InstrumentType.fromString(o.instrument()))`.
3. Load the instrument with `instrumentRepository.findByType(settings.instrument())`. If it's empty, throw `IllegalArgumentException`.
4. Check the fit using MIDI numbers, not `Pitch.compareTo`, which orders by spelling so that A#0 sorts below Bb0. If `instrument.getHighestPitch().toMidiNumber() − interval.getIntervalType().getHalfSteps() < instrument.getLowestPitch().toMidiNumber()`, throw `IllegalArgumentException("Interval <n> does not fit instrument range")`. Otherwise `upper = musicalOperation.getLowerBoundPitchFromInterval(instrument.getHighestPitch(), interval.getIntervalType())`.
5. Pick the start: `start = musicalOperation.getRandomPitch(instrument.getLowestPitch(), upper)`.
6. Compose: `score = composer.single(interval, texture, start, settings)`.
7. Render: `soundRenderingService.render(score, settings.instrument(), format, "interval-" + interval)`.

**`IntervalSoundUseCase.generateIntervalRange`:** steps 1–3 are the same, plus parsing the direction. Then `composer.range(interval, texture, direction, instrument, settings)`, then `render(..., "interval-range-" + interval)`.

**`UpdateSoundSettingsUseCase.update`**

1. Load the settings.
2. Apply `withOverrides(...)`.
3. Check that `instrumentRepository.findByType` finds the instrument; otherwise throw `IllegalArgumentException`.
4. Save.
5. Return the result, mapped the same way as UC-3.

**`SoundSettingsQueryUseCase.getSettings`:** calls `load()`, then passes the result to `SoundSettingsDTOAssembler`, which reads `instrumentRepository.findAll()` and `encoderRegistry.supportedFormats()`.

## 5. Infrastructure layer (`vn.ktt.music.infrastructure`)

### Audio

**`audio/MidiSoundSynthesizer implements ISoundSynthesizerPort`**

This is the only place MIDI appears. It runs `ScoreToMidiSequenceConverter` and then the existing `IMidiRenderer`.

**`audio/midi/ScoreToMidiSequenceConverter`**

- It uses `PPQ = 500` at the default tempo (500,000 µs per quarter note, i.e. 120 BPM), so **1 tick = 1 ms** and the conversion is exact.
- `velocity = round(loudness × 127 / 100)`, so 71 → 90, which is today's `VELOCITY`.
- `InstrumentType` → MIDI program (`PIANO` → 0), sent as a `PROGRAM_CHANGE` at tick 0.
- It writes a single track with no tempo meta event. `HarmonicMidiRenderer` reads tempo only from `tracks[0]` (`HarmonicMidiRenderer.java:119`), and both renderers default to 500,000 µs per quarter note.
- It replaces `MidiSequenceBuilder`.

**`IMidiRenderer`, `HarmonicMidiRenderer`, `Sf2BasedMidiRenderer`:** these return `PcmAudio` instead of `PcmSamples`.

**`Sf2BasedMidiRenderer` bug fix:**

- Lines 85–92 call `AudioSystem.write(..., WAVE, ...)` and then decode 16-bit samples from byte 0. The 44-byte RIFF header is read as about 22 garbage samples, hidden only by the 50 ms fade-in.
- The fix is to read raw PCM straight from the `AudioInputStream` (`readNBytes(frameLength × 2)`) without writing a WAVE container. The decoding goes into a package-private static method, `float[] decodePcm16Le(byte[] pcm, int frames)`, so it can be unit-tested without the SF2 file.

**`audio/encoder/WavAudioEncoder implements IAudioEncoderPort`:** the logic from today's `WavEncoder`, with `format() = WAV`, `mimeType = "audio/wav"` and `extension = "wav"`.

**`audio/AudioGenerationException extends RuntimeException`:**

- It lives in infrastructure only. The domain keeps throwing plain `IllegalArgumentException` / `IllegalStateException`, as CLAUDE.md requires.
- It replaces the render and encode throws at `HarmonicMidiRenderer.java:50`, `Sf2BasedMidiRenderer.java:105` and `WavEncoder.java:35`, and the throws in the new converter.
- The purpose: the planned advice will map `IllegalStateException` to 409 Conflict, but these are infrastructure failures and should stay 500.
- The startup failure at `Sf2BasedMidiRenderer.java:57` stays as it is.

### Persistence

- `persistence/entity/MusicalConfigurationEntity` gains `noteDurationMs`, `silenceMs` and `loudness`, each an `int` column with `nullable = false`. The existing `activeInstrument` becomes the *default* instrument.
- `persistence/MusicalConfigurationRepository` (Spring Data) is renamed and moved to `persistence/gateway/MusicalConfigurationJpaRepository`, matching eartraining's `gateway/*JpaRepository`.
- New: `persistence/gateway/InstrumentJpaRepository`, with `Optional<InstrumentEntity> findByInstrumentType(InstrumentType)`.
- `persistence/gateway/SoundSettingsRepository implements ISoundSettingsRepository`:
  - `load()` maps the first row, or returns `SoundSettings.defaults()` when there are no rows. Today's code throws from `getFirst()` in that case.
  - `save()` updates the existing row, or creates one, and resolves `activeInstrument` through `InstrumentJpaRepository`.
- `persistence/gateway/InstrumentRepository implements IInstrumentRepository` maps entities with `Instrument.reconstruct(musicalEntityFactory, ...)`.
- `import.sql:7` becomes this exact line. It stays on one line, as Hibernate's default import parser requires, and no migration is needed because `ddl-auto=create`:
  `INSERT INTO musical_config (id, active_instrument_id, note_duration_ms, silence_ms, loudness) SELECT gen_random_uuid(), i.id, 188, 21, 71 FROM instruments i WHERE i.instrument_type = 'PIANO';`

### Controllers

Error handling is unchanged: no try/catch, and no `@ControllerAdvice` in this ticket.

**`IntervalsController` (`GET /api/intervals/{interval}/random`)**

- Parameters: `texture`, plus the optional `noteDurationMs`, `silenceMs`, `loudness`, `instrument` and `format`. The optional ones are declared as `@RequestParam(required = false) Integer` or `String`, with no `defaultValue`.
- It builds an `IntervalSoundCommand` and returns `contentType(MediaType.parseMediaType(audio.mimeType()))`, which replaces the hard-coded `audio/wav` at `IntervalsController.java:31`.

**`IntervalRangeController` (`GET /api/interval-range/{interval}`)**

- The same optional parameters as above, plus `direction`.
- The `switch` on direction moves into the use case.
- `MediaType.parseMediaType(audio.mimeType())` replaces line 40.

**New `SoundSettingsController`:** `GET /api/sound-settings` returns a `SoundSettingsDTO`, and `PATCH /api/sound-settings` takes a `SoundSettingsPatch` body and returns a `SoundSettingsDTO`.

Existing URLs and parameters keep working, and responses are unchanged apart from the sub-millisecond timing change noted in section 3.

### Config

- `MusicalDomainServiceConfig` gains the `IIntervalScoreComposer` `@Bean`, and the `IMusicalEntityFactory` `@Bean`, which moves over from eartraining's `DomainServiceConfig`.
- This closes the coupling leak noted in CLAUDE.md. The only classes that inject the factory are `SoundGrpcService`, the two controllers and the deleted adapter; nothing in eartraining does. The bean name stays the same.
- The imports at `DomainServiceConfig.java:13-14` are removed. After that, eartraining imports nothing from `vn.ktt.music`.

## 6. Removed

- `application/sound/IntervalGeneratorService`, `application/sound/inbound/IIntervalGeneratorPort`
- `application/sound/outbound/ISoundGeneratorPort`, `application/sound/dto/IntervalRangeParameters`
- `application/instrument/outbound/IInstrumentConfigurationPort`, `infrastructure/adapter/MusicalConfigurationDataSourceAdapter`
- `infrastructure/audio/MidiSoundGenerator`, `infrastructure/audio/midi/MidiSequenceBuilder`
- `infrastructure/audio/encoder/WavEncoder`, `infrastructure/audio/renderer/PcmSamples`

None of these are referenced from `src/test/java`, which currently holds eartraining tests only. The gRPC contract is unchanged.

## 7. Error handling summary

| Situation | Thrown | HTTP today | HTTP once the planned advice lands |
|---|---|---|---|
| Bad interval, texture, direction, format or instrument; override out of bounds; interval wider than the instrument | `IllegalArgumentException` | 500 | 400 |
| Non-numeric query param (e.g. `loudness=loud`) or malformed PATCH JSON | Spring's `MethodArgumentTypeMismatchException` / `HttpMessageNotReadableException` | 400 | 400 |
| Duplicate encoder for one format | `IllegalStateException` at startup | startup fails | startup fails |
| MIDI, SF2 or encoding failure | `AudioGenerationException` | 500 | 500 |

## 8. Testing

Everything runs under `mvn test` with no database, no SF2 file and no `--add-exports`. The style follows the existing tests: JUnit 5 assertions and hand-written fakes. Mockito, which `spring-boot-starter-test` already provides, is used only for the Spring Data interfaces, because they are too large to fake by hand.

**`SoundSettingsTest`**

- Every bound, in range and out of range.
- A `null` instrument throws.
- `withOverrides` with a `null` argument keeps the current value.
- An override replaces the value.

**`InstrumentTypeTest` / `DirectionTest`:** `fromString` ignores case, and an unknown or `null` value throws `IllegalArgumentException`.

**`IntervalScoreComposerTest`**

- `single` for each texture: the onsets, durations and which note comes first.
- `range` `UP` and `DOWN`: the bases run exactly `lowest … lowest + halfSteps`, in the right order.
- `range` throws when `lowest + 2 × halfSteps > highest`.
- **Golden test:** with `defaults()`, an ascending P5 range has melodic onsets `63 + k·209` ms, and stacked notes last 376 ms and advance by 397 ms.

**`ScoreToMidiSequenceConverterTest`**

- 1 ms = 1 tick at PPQ 500.
- Loudness 71 → velocity 90, 100 → 127, 1 → 1.
- The note-on/note-off pairs are correct.
- A program change is sent at tick 0.
- There is a single track.

**`HarmonicMidiRendererTest`:** a converted score whose last note-off is at `t` ms renders `t × 44.1` samples, ±1. This proves 1 tick = 1 ms end to end.

**`Sf2BasedMidiRendererTest`:** tests the package-private `decodePcm16Le`. The little-endian bytes `0x00 0x40` decode to `0.5f`, `0xFF 0x7F` to about `1.0f`, and `0x00 0x80` to `-1.0f`. No RIFF header is involved.

**`AudioEncoderRegistryTest`:** a duplicate format fails at construction, an unknown format throws `IllegalArgumentException`, and `supportedFormats` is correct.

**`WavAudioEncoderTest`:** the RIFF/WAVE header, a data length of `samples × 2`, the MIME type and the extension.

**`SoundRenderingServiceTest`:** the file name is `baseName + "." + extension`, and the MIME type is passed through.

**`IntervalSoundUseCaseTest`** (with fake repositories, synthesizer, encoder and a fixed-choice `IMusicalOperation`):

- Overrides reach the composer.
- A `null` `overrides` and a `null` `format` fall back to the saved settings and WAV.
- An unsupported format throws.
- An unknown instrument throws.
- An interval wider than the instrument throws.
- `generateInterval` uses the file name `interval-M3.wav`.
- `generateIntervalRange` handles `UP` and `DOWN`, uses the file name `interval-range-M3.wav`, and throws for an unknown direction.

**`SoundSettingsQueryUseCaseTest` / `UpdateSoundSettingsUseCaseTest`**

- The query returns the saved values plus the available instruments and formats as `name()` strings.
- A partial patch is saved, and the fields it doesn't mention are kept.
- An unknown instrument and an out-of-bounds value each throw, and nothing is saved.

**`SoundSettingsRepositoryTest`** (Mockito mocks of the two `*JpaRepository` interfaces):

- An empty table gives `SoundSettings.defaults()`.
- A seeded row is mapped.
- `save` updates the existing row, or creates one when there isn't one.

**Controller tests** (`@WebMvcTest`, which needs the new test dependency `spring-boot-starter-webmvc-test`, confirmed on Maven Central for 4.1.1; ports are replaced with `@MockitoBean`):

- `IntervalsControllerTest`: query overrides and `format` are bound into the command. `Content-Type` and `Content-Disposition` come from `AudioContent`. `loudness=loud` gives 400.
- `IntervalRangeControllerTest`: `direction` is passed through, and the same content checks as above apply.
- `SoundSettingsControllerTest`: `GET` returns the DTO, `PATCH` binds a partial body, and malformed JSON gives 400.

## 9. Suggested build order

`docs/superpowers/plans/2026-09-23-sound-generation.md` turns this into tasks, each of which leaves `mvn test` green.

1. Domain: the value objects, the repository interfaces and `IntervalScoreComposer`, with tests.
2. Application payloads and ports: `AudioFormat`, `PcmAudio`, `EncodedAudio`, `AudioEncoderRegistry` and `SoundRenderingService`, with tests.
3. Infrastructure audio: the converter, `MidiSoundSynthesizer`, the renderers switched to `PcmAudio` along with the SF2 header fix, `WavAudioEncoder` and `AudioGenerationException`.
4. Persistence: the entity columns, the JPA repositories, the gateways and the `import.sql` seed.
5. Use cases and controllers, plus the new `SoundSettingsController`.
6. Move the `IMusicalEntityFactory` bean. Delete the old classes. Update the CLAUDE.md architecture notes, including removing the "Known coupling leak" bullet.

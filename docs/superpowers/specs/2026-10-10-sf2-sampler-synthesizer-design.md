# SF2 sampler synthesizer: design

- **Ticket:** #5 [Refactoring] Improve sound generation
- **Bounded context:** `vn.ktt.music`
- **Status:** approved design (section 1 approved in conversation on 2026-10-10; the rest follows from the decisions recorded below).
- **Supersedes:** `2026-09-23-sound-generation-use-cases-design.md` and its plan. Team feedback on that design: we don't need a domain `Score` entity for now, and the goal is to write our own synthesizer that accepts many input options for realism and outputs PCM.

## 1. Problem, goal and decisions

Today the backend renders interval audio with Java's built-in Gervill synthesizer (`Sf2BasedMidiRenderer`), driven through `javax.sound.midi`. That gives us almost no control over the sound, it needs reflection into `com.sun.media.sound` plus `--add-exports` on every JVM, it loads the 266 MB SoundFont on every request, and it has a header-decoding bug (the WAVE header is read as samples).

**Goal:** our own SoundFont 2 (SF2) sampler synthesizer, written in pure Java. It takes a rich, explicit input (notes plus sound options) and produces PCM. The existing interval endpoints use it with built-in defaults.

### Decisions

| Topic | Decision |
|---|---|
| Engine | Our own SF2 sampler: we parse the `.sf2` and do the voice playback, envelopes, filtering, panning and mixing ourselves. No `javax.sound.*`. |
| Domain `Score` | None. The note list is the synthesizer's input, not a domain concept. |
| Where the synth lives | A self-contained package `vn.ktt.music.infrastructure.synth` with its own API and no dependency on Spring, `javax.sound` or other `vn.ktt` packages. |
| Application boundary | A narrow outbound port: notes plus instrument in, PCM out. An adapter maps it to the synth API and supplies default options. |
| Option groups | All four: instrument and output format; envelope and tuning; rendering quality; effects and expression. Delivered in three phases (section 7). |
| Output | The synth returns float PCM. A separate encoder step turns it into WAV. |

**Out of scope (deferred to a later ticket, once the option set has settled):**

- Saved sound settings, a settings API, and per-request option overrides on the REST endpoints.
- Encoders other than WAV, and an encoder registry.
- Delivering audio to eartraining over gRPC. `sound_service.proto` is unchanged.
- `@RestControllerAdvice` and moving the `IMusicalEntityFactory` bean.

**Ticket acceptance criteria:** (1) sound characteristics become configurable at the synthesizer level; user-facing configuration is the deferred follow-up. (2) The application layer depends only on a port that knows nothing about SF2 or MIDI. (3) SOLID: each engine unit has one job and is tested on its own.

### What the target SoundFont uses

`src/main/resources/soundfonts/grand_piano.sf2` (Steinway D-274, SF2 2.1) was inspected on 2026-10-10. It drives which features land in Phase 1:

- One preset, `Piano`, bank 0 program 0, with 13 preset zones. Each zone selects one instrument by `velRange` (121–127, 111–120, …).
- The 13 instruments share the same 176 samples and differ only in `initialFilterFc`, from 11921 cents (about 8 kHz) down to 6735 cents (about 400 Hz). **Velocity brightness is implemented with the low-pass filter**, so the filter is required in Phase 1.
- 88 stereo pairs: a left sample (type 4) and a right sample (type 2) per key, at 44.1 kHz, about 17 s each. Each key zone has `keyRange` of a single key, `pan` −500 or +500 and a `sampleID`. There are no loops, so `sampleModes` stays at its default.
- Global instrument zones set `releaseVolEnv = −386` timecents (0.8 s).
- No `pmod`/`imod` modulators, no `sm24` chunk, no LFO or modulation-envelope generators.

## 2. Data flow

UC-1 (`GET /api/intervals/{interval}/random`) and UC-2 (`GET /api/interval-range/{interval}`) keep their URLs, parameters and file names. Output becomes stereo WAV (section 5).

```
Controller → IIntervalGeneratorPort (IntervalGeneratorService)
  → IntervalNoteSequencer                          (application)
       → List<SoundNote>   (Pitch pitch, long onsetMs, long durationMs, int velocity)
  → ISoundSynthesizerPort.synthesize(notes, InstrumentType) → PcmAudio
       └─ SamplerSynthesizerAdapter                (infrastructure/audio)
            → SynthRequest(NoteSpec…, SynthOptions defaults)
            → Synthesizer.render(request) → SynthOutput   (infrastructure/synth)
            → PcmAudio
  → IAudioEncoderPort.encode(PcmAudio) → EncodedAudio  (WavAudioEncoder)
  → AudioContent(data, mimeType, fileName)
```

## 3. The synthesizer (`vn.ktt.music.infrastructure.synth`)

### 3.1 Isolation rule

Nothing under `synth` imports `org.springframework`, `javax.sound`, `lombok`, or any `vn.ktt` package outside `vn.ktt.music.infrastructure.synth`. A test enforces this (section 8). Invalid input throws `IllegalArgumentException`. A malformed SoundFont throws `SoundFontFormatException extends RuntimeException`.

### 3.2 Public API (`synth` and `synth.api`)

All of these are records that validate in their compact constructors.

- `Synthesizer` (`synth`): built with `new Synthesizer(SoundFont)`. `SynthOutput render(SynthRequest)`. It keeps no state between calls and is thread-safe.
- `SynthRequest(List<NoteSpec> notes, SynthOptions options)`. `notes` is non-empty and stored as a copy. Phase 3 adds `List<PedalSpan> sustainPedal`.
- `NoteSpec(int midiKey, long onsetMs, long durationMs, int velocity)`: key 0–127, `onsetMs ≥ 0`, `durationMs > 0`, velocity 1–127.
- `SynthOptions`: one optional group per concern. A `null` group means "use the SoundFont's value or the engine default". It's built through `SynthOptions.defaults()` plus `with…` methods, so adding a group in a later phase doesn't break callers. Phase 1 groups:
  - `PresetRef preset(int bank, int program)`: bank 0–16383, program 0–127. Default `(0, 0)`.
  - `OutputFormat output(int sampleRate, ChannelLayout channels, double masterGainDb, long tailMs)`: rate 8000–96000, `MONO`/`STEREO`, gain −60 to +12 dB, tail 0–10000 ms. Default `(44100, STEREO, 0.0, 1000)`.
  - `Interpolation interpolation`: Phase 1 has `LINEAR` only. Phase 2 adds `NEAREST`, `CUBIC` and `SINC`.
- `SynthOutput(float[] samples, int sampleRate, int channels)`: interleaved and within −1…1.

### 3.3 SoundFont loading (`synth.soundfont`)

**`SoundFontLoader.load(Path)` → `SoundFont`**

- It reads the RIFF `sfbk` form and its three lists. From `INFO`, only `ifil` is checked: major version 2, otherwise it throws. From `sdta`, it uses `smpl`; `sm24` is ignored. From `pdta`, it reads `phdr`, `pbag`, `pmod`, `pgen`, `inst`, `ibag`, `imod`, `igen` and `shdr`, using the fixed record sizes 38, 4, 10, 4, 22, 4, 10, 4 and 46 bytes. Each table's terminal record is dropped after it has been used for bag/generator bounds.
- The `smpl` chunk is **memory-mapped** read-only (`FileChannel.map`) and exposed as a little-endian `ShortBuffer`. The 266 MB of sample data never gets copied onto the heap, and the OS pages it in on demand. Absolute `get(int)` on that buffer is safe for concurrent readers.
- It throws `SoundFontFormatException` for a missing chunk, a bad record size, a bag or generator index out of range, a sample whose bounds fall outside `smpl`, or a `smpl` larger than `Integer.MAX_VALUE` bytes.

**Model:**

- `SoundFont`: `findPreset(bank, program)` returns `Optional<Preset>`, plus `sampleData()`.
- `Preset(name, bank, program, Zone globalZone, List<PresetZone> zones)`
- `Instrument(name, Zone globalZone, List<InstrumentZone> zones)`
- `SampleHeader(name, start, end, loopStart, loopEnd, sampleRate, originalPitch, pitchCorrection, sampleType)`
- `Generators`: a `short[]` indexed by `GeneratorType`, which holds the SF2 generator ids 0–60 with their defaults. `keyRange`/`velRange` are decoded as lo/hi bytes.

A zone is global if it is the first zone of its preset or instrument and has no terminal generator (`instrument` for presets, `sampleID` for instruments). Other zones without a terminal generator are dropped, as SF2 2.04 §7.3 and §7.7 require.

### 3.4 Zone resolution (`synth.engine.ZoneResolver`)

For a note (key, velocity) and a preset, produce one `VoiceSpec` for every pair (preset zone, instrument zone) whose key and velocity ranges both contain the note. The stereo piano therefore produces two voices per note, one panned hard left and one hard right, without any special stereo logic.

**Generator value for a pair:**

- Instrument level is **absolute**. Use the local zone's value, falling back to the instrument's global zone and then the SF2 default.
- Preset level is **additive**. Add the local preset zone's value, falling back to the preset's global zone, otherwise 0.
- Generators that are not valid at preset level are ignored there. These are the address offsets, `keynum`, `velocity`, `sampleModes`, `exclusiveClass` and `overridingRootKey`.
- Each result is clamped to the SF2 2.04 §8.1.3 range.

**Sample addressing:**

- Start = `shdr.start + startAddrsOffset + 32768 × startAddrsCoarseOffset`. End and the loop points work the same way with their own offset pairs.
- The result is clamped to `[shdr.start, shdr.end]`.
- A ROM sample (`sampleType & 0x8000`) produces no voice.

**Default modulators:**

- Only **velocity → initial attenuation** applies: `960 × concave(1 − v/127)` cB, where `concave(x) = clamp(−(5/12)·log10(1 − x), 0, 1)` and `concave(1) = 1`. So velocity 127 adds 0 cB, 90 adds about 59.8 cB and 1 adds about 842 cB.
- The SF2 default velocity → filter cutoff modulator is **not** applied. SF2 2.01 and 2.04 define it differently, and this SoundFont already encodes velocity brightness through its per-layer `initialFilterFc`. Applying both would darken soft notes twice.
- The controller-driven defaults (CC1, CC7, CC10, CC91, CC93, pitch wheel, channel pressure) don't apply, because the synth has no MIDI controllers.
- `pmod`/`imod` are parsed and ignored.

### 3.5 Voice rendering (`synth.engine`)

Each `VoiceSpec` becomes a `Voice` that renders one sample per output frame into stereo mix buffers. The per-voice chain is: interpolated sample read, then the low-pass filter, then the volume envelope and attenuation gain, then the pan gains.

**Pitch:**

- Key = the `keynum` generator if it's set, otherwise the note's key.
- Root = `overridingRootKey` if it's ≥ 0, otherwise `originalPitch`. If `originalPitch` is 255, the root is 60.
- `cents = (key − root) × scaleTuning + 100 × coarseTune + fineTune + pitchCorrection`
- Playback step = `2^(cents/1200) × sample.sampleRate / output.sampleRate`, in samples per output frame, tracked as a `double` phase.
- Velocity = the `velocity` generator if it's set, otherwise the note's velocity.

**Interpolation (`Interpolator`):** `LINEAR` reads between `floor(phase)` and the sample after it. Reads past the voice's end return 0.

**Loop (`sampleModes`):**

- 0 or 2: no loop. The voice ends at the sample end.
- 1: the voice loops `[loopStart, loopEnd)` until it ends.
- 3: the voice loops until release, then plays on to the end.

**Volume envelope (`VolumeEnvelope`)**, from SF2 2.04 §8.1.2 and §9.1.7:

- Time from timecents is `2^(tc/1200)` s. The default of −12000 tc is about 1 ms.
- Delay: silent for `delayVolEnv`.
- Attack: amplitude rises **linearly** from 0 to 1 over `attackVolEnv`.
- Hold: stays at 0 dB for `holdVolEnv + keynumToVolEnvHold × (60 − key)` tc.
- Decay: attenuation rises **linearly in cB** at 1000 cB per `decayVolEnv + keynumToVolEnvDecay × (60 − key)` tc, until it reaches `sustainVolEnv` cB.
- Sustain: attenuation stays at `sustainVolEnv`.
- Release: starts at note-off, from whatever level the envelope is at. A level reached during attack is converted with `−200·log10(amplitude)`. Attenuation then rises at 1000 cB per `releaseVolEnv`.
- The voice is finished once its attenuation reaches 960 cB (−96 dB).

**Gain:** amplitude = `envelopeAmplitude × 10^(−(initialAttenuation + velocityAttenuation)/200)`. Sample values are `short / 32768f`.

**Low-pass filter (`LowPassFilter`):**

- It's a 2-pole resonant RBJ biquad low-pass, in direct form I with `double` state.
- Cutoff in Hz = `8.176 × 2^(initialFilterFc/1200)`.
- Q = `10^((initialFilterQ/10 − 3.01)/20)`, so the default 0 cB gives Butterworth, 0.707.
- The filter is **bypassed** when `initialFilterFc ≥ 13500` or the cutoff is ≥ 0.45 × the output rate.

**Pan:**

- `p = pan / 500` in −1…1, and `θ = (p + 1)·π/4`, giving gains `L = cos θ` and `R = sin θ` (constant power).
- So a hard-left voice is (1, 0) and a centered voice is (0.707, 0.707).

### 3.6 Mixing and output (`synth.engine.Mixer`)

- Frames are counted as `round(ms × sampleRate / 1000)`.
- A note's **release time** is `onsetMs + durationMs`. Phase 3 extends this with the sustain pedal.
- Buffer length = `max(release time) + tailMs`. It's deterministic and doesn't depend on how long voices actually ring.
- Voices that finish early stop rendering. Voices still sounding at the end of the buffer are cut, and a 5 ms linear fade-out is applied to the last frames of the whole buffer.
- `masterGainDb` is applied, then a **soft limiter**: `|x| ≤ 0.8` passes through unchanged, and above that `y = sign(x)·(0.8 + 0.2·tanh((|x| − 0.8)/0.2))`. The output stays within −1…1 and has a continuous slope.
- `STEREO` output interleaves L and R. `MONO` outputs `(L + R) / 2`.
- If no zone matches any note (for example, a key outside the preset's ranges), the result is silent output of the computed length. This matches SF2 synth behavior.

### 3.7 Unsupported SF2 features (documented, not errors)

These are parsed and ignored: the modulation envelope, the modulation and vibrato LFOs, custom modulators, `exclusiveClass`, `sm24` and the `INFO` strings. The target SoundFont uses none of them. Phase 3 adds `chorusEffectsSend`/`reverbEffectsSend`.

## 4. Application layer (`vn.ktt.music.application.sound`)

**Payloads (`sound/audio`), as records:**

- `SoundNote(Pitch pitch, long onsetMs, long durationMs, int velocity)`: requires a non-null pitch, `onsetMs ≥ 0`, `durationMs > 0` and velocity 1–127.
- `PcmAudio(float[] samples, int sampleRate, int channels)`: requires `channels` of 1 or 2, `samples.length % channels == 0` and `sampleRate > 0`.
- `EncodedAudio(byte[] data, String mimeType, String extension)`

**Outbound ports (`sound/outbound`):** these replace `ISoundGeneratorPort`.

- `ISoundSynthesizerPort`: `PcmAudio synthesize(List<SoundNote> notes, InstrumentType instrument)`
- `IAudioEncoderPort`: `EncodedAudio encode(PcmAudio pcm)`

**`IntervalNoteSequencer` (`@Component`):** this takes over the timing rules of `MidiSequenceBuilder`, converted from ticks to whole milliseconds. The old tick was 1.0417 ms.

| Constant | Old | New |
|---|---|---|
| `PREROLL_MS` | 60 ticks (62.5 ms) | 63 |
| `NOTE_MS` | 180 ticks (187.5 ms) | 188 |
| `STACKED_NOTE_MS` | 360 ticks (375 ms) | 376 |
| `GAP_MS` | 20 ticks (20.8 ms) | 21 |
| `VELOCITY` | 90 | 90 |

- `List<SoundNote> single(Pitch lower, Interval interval, Interval.Texture texture)`:
  - `ASCENDING`: lower at 63, then upper at 63 + 188 + 21.
  - `DESCENDING`: the same, with the upper note first.
  - `STACKED`: both notes at 63, held for 376 ms.
- `List<SoundNote> range(Interval interval, Interval.Texture texture, boolean descendingSweep, Instrument instrument)`:
  - Bases run from `lowest` to `min(lowest + h, highest − h)` in MIDI numbers, where `h` is the interval's half steps. They run in reverse when `descendingSweep` is true.
  - Melodic steps advance by `NOTE_MS + GAP_MS`, and stacked steps by `STACKED_NOTE_MS + GAP_MS`.
  - If the upper bound is below `lowest`, it throws `IllegalArgumentException("Interval <n> does not fit instrument range")`. This replaces the old clamp to MIDI 108. With the A0–C8 seed, the result is the same as today.
- The upper pitch is `lower.getPitchAfterHalfSteps(h)`.

**`IntervalGeneratorService`:** keeps `IIntervalGeneratorPort` and its three methods, so the controllers' calls don't change.

- `generateInterval` picks the start pitch as it does today, then runs `sequencer.single(…)`.
- `generateUpwardInterval` and `generateDownwardInterval` run `sequencer.range(…, false/true, instrument)`.
- All three then call `render(notes, instrument.getInstrumentType(), baseName)`:

  ```
  pcm     = synthesizer.synthesize(notes, instrumentType)
  encoded = encoder.encode(pcm)
  return new AudioContent(encoded.data(), encoded.mimeType(), baseName + "." + encoded.extension())
  ```

**`AudioContent`:** a record `(byte[] data, String mimeType, String fileName)` that replaces the Lombok class. The unused `fileSize` is dropped.

## 5. Infrastructure wiring

**`infrastructure/audio/SamplerSynthesizerAdapter implements ISoundSynthesizerPort` (`@Component`):**

- It maps `SoundNote` to `NoteSpec(pitch.toMidiNumber(), onsetMs, durationMs, velocity)`.
- It maps `InstrumentType` to a `PresetRef` through a `Map<InstrumentType, PresetRef>` (`PIANO` → `(0, 0)`). An unmapped type throws `IllegalArgumentException`.
- It uses `SynthOptions.defaults()`: 44.1 kHz, **stereo**, 0 dB, 1000 ms tail, `LINEAR`.
  - Output changes from mono to stereo, because the SoundFont is recorded in stereo.
  - The tail drops from Gervill's 2 s to 1 s, because the 0.8 s release finishes inside it.
- It maps `SynthOutput` to `PcmAudio`.

**`infrastructure/audio/encoder/WavAudioEncoder implements IAudioEncoderPort` (`@Component`):**

- It writes a 44-byte RIFF/WAVE header by hand (PCM format 1, 16-bit, 1 or 2 channels), followed by little-endian samples. Each sample is `Math.round(clamp(x, −1, 1) × 32767)`.
- MIME type `audio/wav`, extension `wav`. No `javax.sound`.

**`infrastructure/config/SynthesizerConfig` (`@Configuration`):**

- A `@Bean SoundFont` loaded from `soundfont.path`, which stays `classpath:soundfonts/grand_piano.sf2`. If the `Resource` is a file, it is mapped in place. Otherwise, for example inside a jar, it's copied once to a temp file (`deleteOnExit`) and that file is mapped.
- A missing resource fails startup with an `IllegalStateException` that names the path. The SoundFont is now **loaded at startup**, not on every request.
- A `@Bean Synthesizer`.

**Controllers:** they use `audio.data()`, `audio.fileName()` and `MediaType.parseMediaType(audio.mimeType())` instead of the hard-coded `audio/wav`. Nothing else changes.

**`pom.xml`:** remove the `--add-exports` `jvmArguments` from `spring-boot-maven-plugin`.

**Removed:**

- `infrastructure/audio/MidiSoundGenerator`, `audio/midi/MidiSequenceBuilder`
- `audio/renderer/IMidiRenderer`, `HarmonicMidiRenderer`, `Sf2BasedMidiRenderer`, `PcmSamples`
- `audio/encoder/WavEncoder`
- `application/sound/outbound/ISoundGeneratorPort`, `application/sound/dto/IntervalRangeParameters`

None of these are referenced from `src/test/java`. After this change nothing in the project imports `javax.sound`.

**CLAUDE.md:** update the audio description (SF2 sampler, notes → PCM → WAV) and remove the `--add-exports` notes. Also note that the app now fails to start without the SF2 file.

## 6. Error handling

| Situation | Thrown | HTTP today | HTTP once the planned advice lands |
|---|---|---|---|
| Invalid `NoteSpec`/options, unknown preset, unmapped instrument, interval that doesn't fit | `IllegalArgumentException` | 500 | 400 |
| Malformed SoundFont | `SoundFontFormatException` at startup | startup fails | startup fails |
| SoundFont resource missing | `IllegalStateException` at startup | startup fails | startup fails |

Rendering does no I/O after startup and encoding writes to memory only, so there's no runtime audio-failure exception. `AudioGenerationException` from the old design is dropped.

## 7. Delivery phases

Each phase ships behind the same `SynthRequest` → `SynthOutput` contract and leaves `mvn test` green. **Each phase gets its own implementation plan.** Phase 2 and 3 plans are written against the code that Phase 1 lands.

### Phase 1: replace Gervill

Everything in sections 3–6. When it's done, the app runs on our own synthesizer and all MIDI and `javax.sound` code is gone.

### Phase 2: envelope, tuning and rendering quality

New `SynthOptions` groups:

- **`EnvelopeOverride(Double delayMs, Double attackMs, Double holdMs, Double decayMs, Double sustainDb, Double releaseMs)`**
  - Each non-null field replaces the resolved volume-envelope value for every voice.
  - Times are 0–20000 ms. `sustainDb` is an attenuation of 0–144 dB.
- **`Tuning(double a4Hz, double fineCents)`**
  - `a4Hz` is 400–480 and `fineCents` is −100 to +100.
  - This adds `1200·log2(a4Hz/440) + fineCents` to every voice's pitch.
- **`FilterOverride(Double cutoffShiftCents, Double resonanceDb)`**
  - The cutoff shift is **relative**, −9600 to +9600 cents, so the per-velocity-layer brightness of this SoundFont is preserved.
  - `resonanceDb` (0–96) replaces `initialFilterQ`.
- **`Double balance`** (−1…1): added to each voice's pan and clamped. It's relative for the same reason: an absolute pan would collapse the stereo pairs.
- **Interpolation:**
  - `NEAREST`.
  - `CUBIC`: 4-point Catmull-Rom.
  - `SINC`: windowed sinc with 16 taps and a Kaiser window (β = 8), using a precomputed table with 256 phases.
- The adapter default changes to `CUBIC`.

### Phase 3: effects and expression

- **`SynthRequest.sustainPedal: List<PedalSpan(long downMs, long upMs)>`**
  - The spans must be sorted, non-overlapping and have `up > down`.
  - A note whose release time falls inside a span releases at that span's `upMs`.
  - Re-striking a held key adds a voice and leaves the old one ringing.
  - The buffer length uses the deferred release times.
- **`ReverbOptions(double send, double roomSize, double damping, double wet, double width)`**, each 0–1:
  - It's a Freeverb design: 8 parallel feedback combs with damping, then 4 series allpasses per channel, and a 23-sample stereo spread. The tunings are defined at 44.1 kHz and scaled to the output rate.
  - Per-voice send = `clamp(reverbEffectsSend/1000 + send, 0, 1)`.
  - Output = dry + `wet ×` the reverb of the send bus.
- **`ChorusOptions(double send, double rateHz, double depthMs, double wet)`**: a 3-voice modulated delay line with the same send rule.
- The `MONO` downmix happens after the effects.

## 8. Testing (Phase 1)

Everything runs under `mvn test` with no database and no SF2 file. The style follows the existing tests: JUnit 5 and hand-written fakes.

**Fixture: `TestSoundFontBuilder`** (test sources, `synth` test package)

- It builds a valid SF2 in memory and writes it to a `@TempDir` file.
- It lets tests configure presets, instruments, zones (generators by `GeneratorType`) and samples given as `short[]`.
- Typical samples: a constant level (for exact gain and envelope checks), a ramp (for interpolation and addressing) and a sine at a known frequency (for pitch).

**Unit tests:**

- **`SynthPackageIsolationTest`:** scans `src/main/java/vn/ktt/music/infrastructure/synth/**` and fails on any forbidden import (section 3.1).
- **API records:** every bound, and each `SynthOptions` default.
- **`SoundFontLoaderTest`:**
  - It parses the fixture's presets, global and local zones, and sample headers.
  - It rejects a wrong form type, `ifil` major version ≠ 2, a missing `pdta` chunk, a truncated record, and sample bounds outside `smpl`.
- **`ZoneResolverTest`:**
  - Instrument values are absolute: local beats global beats default.
  - Preset values are additive.
  - Key and velocity ranges must both match.
  - Two matching zones give two voices.
  - Generators that are invalid at preset level are ignored.
  - Address offsets work, including coarse offsets.
  - A ROM sample gives no voice.
  - Velocity attenuation is 0 cB at 127, 59.8 cB at 90 (±0.1) and 842 cB at 1 (±1).
- **`UnitsTest`:** timecents to seconds, cB to amplitude, and absolute cents to Hz (`13500 → 19912 Hz ± 1`, `6900 → 440 Hz`).
- **`InterpolatorTest`:** `LINEAR` on a ramp gives exact midpoints, and reads past the end give 0.
- **`VolumeEnvelopeTest`:**
  - The attack is linear.
  - Hold length includes the keynum scaling.
  - Decay reaches sustain at the expected frame.
  - Release from sustain takes `release × (960 − sustain)/1000` s.
  - Release during attack starts from the current level.
  - `finished()` turns true at 960 cB.
- **`LowPassFilterTest`:**
  - DC passes with gain 1 (±1e-3).
  - A sine one octave above an 8 kHz cutoff is attenuated by ≥ 10 dB.
  - The filter is bypassed at 13500 cents.
- **`VoiceTest`:**
  - With root = key, the step is exactly 1.0 at 44.1 kHz.
  - One octave up gives step 2.0.
  - A 22050 Hz sample rendered at 44.1 kHz gives step 0.5.
  - `pitchCorrection` and `scaleTuning` are applied.
  - Loop modes 0, 1 and 3 behave as specified.
  - The pan gains are (1, 0), (0.707, 0.707) and (0, 1).
- **`SynthesizerTest`** (end to end on the fixture):
  - The buffer length is `max(onset + duration) + tail` in frames.
  - Stereo interleaving and mono downmix are correct.
  - A constant-level sample at velocity 127 with no filter gives the expected steady amplitude.
  - An unknown preset throws `IllegalArgumentException`.
  - A key with no zone gives silence.
  - The limiter keeps loud overlapping notes within −1…1.
  - The last 5 ms fade to 0.
- **`GrandPianoSmokeTest`:**
  - It runs only when `src/main/resources/soundfonts/grand_piano.sf2` exists (`assumeTrue`).
  - It loads the real file and renders C4 at velocity 90.
  - It asserts stereo output that isn't silent, a peak < 1, a correct length, and that a velocity-30 note has lower RMS than a velocity-120 note.
- **`IntervalNoteSequencerTest`:**
  - Each texture for `single`.
  - `range` ascending and descending.
  - It throws when the interval doesn't fit.
  - **Golden test:** an ascending P5 range on A0–C8 has onsets `63 + k·209` ms and 8 bases (A0 through E1, MIDI 21–28).
- **`IntervalGeneratorServiceTest`** (fake sequencer inputs, a fake synthesizer and encoder, and a fixed-choice `IMusicalOperation`):
  - The file names are `interval-M3.wav` and `interval-range-M3.wav`.
  - The MIME type is passed through.
  - The up and down sweeps reach the sequencer.
- **`SamplerSynthesizerAdapterTest`:**
  - Notes map to MIDI keys.
  - `PIANO` maps to preset `(0, 0)`.
  - Default options are used, and the output maps to `PcmAudio`. The test runs on the fixture SoundFont.
- **`WavAudioEncoderTest`:** the header fields for mono and stereo, a data length of `samples × 2`, clamping and rounding, the MIME type and the extension.

**Manual verification (end of Phase 1):** start the app with the real SoundFont and `curl` both endpoints for each texture. Listen to the results and compare them with the Gervill output from `main`: velocity brightness, the stereo image and the release should all be audible.

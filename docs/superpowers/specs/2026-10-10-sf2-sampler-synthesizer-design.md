# SF2 sampler synthesizer: design

- **Ticket:** #5 [Refactoring] Improve sound generation
- **Bounded context:** `vn.ktt.music`
- **Status:** approved design (section 1 approved in conversation on 2026-10-10; the rest follows from the decisions recorded below). Expanded on 2026-10-11 with a walkthrough of how the parts work together (section 2) and the reasoning behind every class's fields (section 9).
- **How to read this:** section 2 explains the system end to end and is enough to understand it. Sections 3–8 are the precise contract that the implementation plan builds against. Section 9 explains why each class has the fields it has.
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

## 2. How it works together

### 2.1 The request path through the app

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

Each step answers exactly one question, and nothing else in the chain needs to know that answer:

| Step | Question it answers | Knows nothing about |
|---|---|---|
| `IntervalNoteSequencer` | *Which* notes, *when*, and for how long? (musical timing) | SoundFonts, sample rates, file formats |
| `SamplerSynthesizerAdapter` | Which SoundFont preset is "piano", and which sound options apply today? | Intervals, textures, HTTP |
| `Synthesizer` | What do these notes *sound* like? (notes → PCM) | Intervals, `Pitch`, Spring, file formats |
| `WavAudioEncoder` | How is PCM stored in a file? | Notes, instruments, SoundFonts |
| `IntervalGeneratorService` | Puts the steps together and names the file | How any single step works |

### 2.2 SoundFont 2 in brief

A SoundFont (`.sf2`) is a bundle of **recorded samples** plus **instructions for playing them**. A synthesizer that reads SoundFonts is called a *sampler*: it doesn't invent the sound, it plays back recordings, adjusting their pitch, loudness, brightness and position. That's why it can sound like a real Steinway, where a hand-written oscillator like the old `HarmonicMidiRenderer` cannot.

The file is a tree:

```
SoundFont
 ├─ smpl chunk ........ every sample's 16-bit audio, stored back to back
 └─ presets ........... what a user picks, addressed by (bank, program); here (0, 0) = "Piano"
     └─ preset zones ...... "for notes in this key/velocity range, use instrument X" (+ additive tweaks)
         └─ instrument ........ a named group of zones
             └─ instrument zones .. "for notes in this key/velocity range, play sample Y" (+ absolute settings)
                 └─ sample header .. where Y's audio sits in smpl, its root key, its rate, its loop points
```

- **Zones and generators.** A *zone* is a rule: "for notes in these key and velocity ranges, use this target and set these parameters". The parameters are called *generators*. There are about 60 of them (pan, filter cutoff, envelope times, tuning, …). If the first zone of a preset or instrument has no target, it is the **global zone**, and it supplies defaults for its sibling zones.
- **One note can match several zones.** Each match becomes its own **voice**, which is one playing sample. The stereo piano relies on this: each key has a left-sample zone and a right-sample zone, so one note becomes two voices panned hard left and hard right.
- **Velocity layers.** Preset zones that differ only by velocity range let one preset sound different when struck harder. In our piano the 13 layers differ only in filter cutoff, so soft notes are darker (section 1).

SF2 uses its own units. The engine keeps them as long as it can (see 2.5):

| Unit | Used for | Reference points |
|---|---|---|
| cents | pitch offsets | 100 = one semitone, 1200 = one octave |
| absolute cents | filter cutoff | 6900 = 440 Hz; 13500 ≈ 20 kHz (the default, "filter off") |
| timecents | envelope times | 0 = 1 s, −1200 = 0.5 s, −12000 ≈ 1 ms (the default) |
| centibels (cB) | attenuation (how much quieter) | 10 cB = 1 dB; 200 cB = amplitude ×0.1; 960 cB = treated as silence |
| 0.1 % | pan, effect sends | pan −500 = hard left, 0 = center, +500 = hard right |

### 2.3 Inside the synthesizer

```
synth/
  Synthesizer ─────────► engine.Mixer ─────► engine.ZoneResolver ─────► soundfont.Preset, Zone, …
  (public facade)            │                 (note → VoiceSpecs)
                             └──────────────► engine.Voice ─► Interpolator, LowPassFilter,
       api.SynthRequest ─► (in)                (one per VoiceSpec)   VolumeEnvelope, Units
       api.SynthOutput  ◄─ (out)
  soundfont.SoundFontLoader ─► soundfont.SoundFont   (once, at startup)
```

The work happens in four stages, at very different frequencies:

| Stage | Class | Input → output | How often |
|---|---|---|---|
| **Load** | `SoundFontLoader` | `.sf2` file → `SoundFont` (preset tree on the heap, samples memory-mapped) | Once per app start |
| **Resolve** | `ZoneResolver` | (preset, key, velocity) → `List<VoiceSpec>` | Once per note |
| **Render** | `Voice` (+ `Interpolator`, `LowPassFilter`, `VolumeEnvelope`) | `VoiceSpec` → samples added into the stereo mix buffers | Once per voice, looping over every output frame |
| **Finish** | `Mixer` | stereo mix buffers → master gain → limiter → fade-out → interleave/downmix → `SynthOutput` | Once per request |

The render stage is the only hot loop: about 44,100 iterations per second of sound for each voice. Everything is arranged so that the loop does as little as possible: read, filter, multiply and add.

### 2.4 Walkthrough: one request end to end

Take `GET /api/intervals/M3/random?texture=ascending`, and suppose the random start pitch is C4.

1. **Sequencing (application).** `IntervalNoteSequencer.single(C4, M3, ASCENDING)` returns two `SoundNote`s at velocity 90:
   - C4 at 63 ms for 188 ms
   - E4 at 63 + 188 + 21 = 272 ms for 188 ms
2. **Adapting (infrastructure).** `SamplerSynthesizerAdapter` turns each `Pitch` into a MIDI key and builds `SynthRequest([NoteSpec(60, 63, 188, 90), NoteSpec(64, 272, 188, 90)], defaults().withPreset((0, 0)))`. The defaults are 44.1 kHz, stereo, 0 dB, a 1000 ms tail and `LINEAR` interpolation.
3. **Sizing the buffer (`Mixer`).** The last release is at 272 + 188 = 460 ms. Add the 1000 ms tail and the buffer is 1460 ms, or 64,386 frames. Two `float[64386]` buffers are allocated, left and right. They belong to this request only.
4. **Resolving C4 (`ZoneResolver`).** The resolver walks the piano preset:
   - Of the 13 preset zones, exactly one has a velocity range containing 90. That zone's instrument is one velocity layer.
   - In that instrument, the zones whose key range contains 60 are the left and right samples of C4. That gives **two `VoiceSpec`s**.
   - For each one, the generators are combined (instrument value, plus the preset value, clamped) and turned into a ready-to-play description:
     - `pitchCents = (60 − 60) × 100 = 0`, because the sample was recorded at C4.
     - `attenuationCb = 0 + 59.8` from the velocity curve, which is ×0.502 amplitude.
     - `filterCutoffCents` comes from the layer.
     - `pan = −500` or `+500`.
     - The release is −386 timecents (0.8 s).
5. **Rendering C4 (`Voice`, twice).** Each voice is constructed with start frame `round(63 × 44.1) = 2778` and release frame `round(251 × 44.1) = 11069`. Its fixed numbers are worked out once, before the loop:
   - the playback step: 1.0, because the key equals the root and both rates are 44.1 kHz
   - the gain: 0.502
   - the pan gains: (1, 0) for the left voice and (0, 1) for the right
   - the filter coefficients
   - the envelope in frames

   Then, for every frame from 2778 on, it does the following:
   - reads the sample at the current position (`Interpolator`, `LINEAR`)
   - filters it (`LowPassFilter`)
   - multiplies by the envelope level (`VolumeEnvelope.next()`) and the gain
   - adds the result into the left and right buffers with the pan gains
   - advances the position by the step

   At frame 11069 the envelope enters release. It reaches −96 dB 0.768 s later, around frame 44,938, and the voice stops.
   - The piano's natural decay while the key is held is part of the recording itself. The envelope only shapes the release.
6. **Resolving and rendering E4.** The same happens for E4, with two more voices starting at frame 11,995. Their tails overlap the end of C4's release. That overlap is just addition into the same buffers.
7. **Finishing (`Mixer`).** Every sample is multiplied by the master gain (×1 at 0 dB) and passed through the soft limiter (unchanged here, because the peaks are around 0.23). The last 5 ms fade to zero. Left and right are then interleaved into one `float[128772]`, which becomes the `SynthOutput(samples, 44100, 2)`.
8. **Back out.** The adapter wraps the same array in a `PcmAudio`, without copying it. `WavAudioEncoder` writes a 44-byte header plus 16-bit samples (257,588 bytes). `IntervalGeneratorService` names the file `interval-M3.wav`, and the controller returns it as `audio/wav`.

The whole render takes a few milliseconds. The 266 MB SoundFont is never read in full: only the pages holding C4's and E4's samples are touched.

### 2.5 Principles behind the design

These principles explain most of the field choices in section 9.

1. **Resolve once, render many.** Anything that is constant for a voice (pitch step, gain, pan gains, filter coefficients, stage lengths) is computed before the per-frame loop and stored in fields. The loop itself never looks at generators, zones or units.
2. **Keep SF2 units until the output rate is known.** Envelope times and filter cutoffs stay in timecents and absolute cents through `VoiceSpec`. They only become frames and filter coefficients inside `Voice`, because both depend on the request's output sample rate. The same resolved voice can therefore render at 22.05 kHz or 48 kHz.
3. **Offline, whole-buffer rendering.** This is not a real-time synth: every note is known before rendering starts. So the buffer length is computed up front, voices are rendered one at a time (voice-major), and later effects such as reverb (Phase 3) can run over whole buffers. A real-time design would need a block scheduler and is unnecessary here.
4. **Immutable inputs, per-request state.** Requests, options and the SoundFont model are immutable. Each request allocates its own buffers and voices. The only shared thing is the read-only, memory-mapped sample buffer, which is read with absolute `get(int)` only. That is the whole thread-safety story; there are no locks.
5. **Two models on purpose.** The application has `SoundNote` and `PcmAudio`; the synth has `NoteSpec` and `SynthOutput`. They look alike, but the application must not depend on infrastructure types (hexagonal architecture), and the synth must not depend on application types (so it can move into its own service). The adapter is the only class that knows both. A little duplication is the price of that freedom.
6. **Deterministic output.** The synth has no randomness: the same request always produces the same samples. The random start pitch is chosen in the application, before the synth is called. Determinism is what makes exact-value tests possible.

## 3. The synthesizer (`vn.ktt.music.infrastructure.synth`)

### 3.1 Isolation rule

Nothing under `synth` imports `org.springframework`, `javax.sound`, `lombok`, or any `vn.ktt` package outside `vn.ktt.music.infrastructure.synth`. A test enforces this (section 8). Invalid input throws `IllegalArgumentException`. A malformed SoundFont throws `SoundFontFormatException extends RuntimeException`.

### 3.2 Public API (`synth` and `synth.api`)

`Synthesizer` is a class, and `ChannelLayout` and `Interpolation` are enums. Everything else is a record that validates in its compact constructor. Section 9 explains each field.

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

- `SoundFont`: `findPreset(bank, program)` returns `Optional<Preset>`, plus `presets()` and `sampleData()`.
- `Preset(name, bank, program, Zone globalZone, List<PresetZone> zones)` and `PresetZone(Zone zone, Instrument instrument)`
- `Instrument(name, Zone globalZone, List<InstrumentZone> zones)` and `InstrumentZone(Zone zone, SampleHeader sample)`. A `null` global zone becomes `Zone.EMPTY`. Zones hold direct references to their instrument or sample, which are resolved at load time.
- `SampleHeader(name, start, end, loopStart, loopEnd, sampleRate, originalPitch, pitchCorrection, sampleType)`
- `GeneratorType`: an enum of the generators the engine reads, each with its SF2 id, default, legal range and whether it may appear at preset level. Ids not in the enum are skipped while parsing.
- `Zone`: the generators **actually set** on one zone, as an `EnumMap<GeneratorType, Integer>`. `get` falls back to the SF2 default. `keyRange`/`velRange` stay packed as SF2 stores them (low byte, high byte) and are decoded by helpers. Section 9 explains why this replaced an earlier design that used a `short[]` filled with defaults.

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
- Root = `overridingRootKey` if it's ≥ 0, otherwise `originalPitch`. If `originalPitch` is above 127 (SF2 uses 255 for unpitched samples), the root is 60.
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
- Buffer length = `max(release time) + tailMs`. It's deterministic and doesn't depend on how long voices actually ring. A request longer than `Mixer.MAX_DURATION_MS = 600000` ms (10 minutes) throws `IllegalArgumentException`, so a bad onset can't allocate gigabytes.
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
| Invalid `NoteSpec`/options, unknown preset, unmapped instrument, interval that doesn't fit, render longer than 10 minutes | `IllegalArgumentException` | 500 | 400 |
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
  - The pitch in cents combines key, root, `scaleTuning`, `coarseTune`, `fineTune` and `pitchCorrection`; `overridingRootKey` and an unpitched `originalPitch` are handled.
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
  - A request longer than 10 minutes throws `IllegalArgumentException`.
  - Concurrent renders of the same request on several threads give identical output.
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

**`SynthesizerConfigTest`:** loads from a file location, fails with `IllegalStateException` naming a missing path, and copies a non-file resource (as inside a jar) to a temp file before loading.

**Manual verification (end of Phase 1):** start the app with the real SoundFont and `curl` both endpoints for each texture. Listen to the results and compare them with the Gervill output from `main`: velocity brightness, the stereo image and the release should all be audible.

## 9. Class reference: fields and the reasoning behind them

This section covers every Phase 1 class, grouped by package: what each field holds and why it is there. The general principles are in 2.5; they are referred to here as P1–P6.

### 9.1 Synth API (`synth.api`): what a caller can say

The API is the synthesizer's contract with the outside world. Its fields are chosen to be **sufficient to describe a sound, and nothing more**, in units a caller can reason about.

**`NoteSpec(int midiKey, long onsetMs, long durationMs, int velocity)`**: one note to play.

| Field | Why it exists, and why this type |
|---|---|
| `midiKey` (0–127) | SF2 selects zones by MIDI key number (`keyRange`), so a key number is exactly what the engine needs. The synth deliberately doesn't use the domain's `Pitch`: spelling (C♯4 vs D♭4) doesn't change the sound, and importing `Pitch` would break isolation (3.1). |
| `onsetMs` (≥ 0) | When the note starts, as **absolute milliseconds** from the start of the audio. There's no tempo, bar or tick concept: those belong to whoever composes the notes. Milliseconds are converted to frames only at render time, so the same request works at any output sample rate (P2). `long`, so long timelines never overflow. |
| `durationMs` (> 0) | How long the key is held, which is when the release starts. It's a duration rather than an end time, so it can't be before the onset. `releaseMs()` derives the end. |
| `velocity` (1–127) | How hard the key is struck. SF2 defines velocity layers (`velRange`) and the default loudness curve on MIDI velocity, so a velocity is needed to pick the right recording. **0 is excluded** because MIDI treats a note-on with velocity 0 as a note-off. This replaces the old design's `loudness` 1–100: loudness would have to be converted back to velocity anyway, losing layer precision. |

There is no per-note instrument or channel: Phase 1 plays one preset per request. A per-note preset could be added later as an optional field without breaking callers.

**`PresetRef(int bank, int program)`**: which sound to use.

| Field | Why |
|---|---|
| `bank` (0–16383) | SF2 addresses presets by (bank, program), following MIDI. 16383 is the largest 14-bit MIDI bank number. |
| `program` (0–127) | The MIDI program number within the bank. |

It doesn't use the preset's name, because SF2 names aren't required to be unique and (bank, program) is the standard address. The mapping "PIANO → (0, 0)" lives in the adapter, so the synth never learns the app's instrument names.

**`OutputFormat(int sampleRate, ChannelLayout channels, double masterGainDb, long tailMs)`**: the shape and level of the result.

| Field | Why |
|---|---|
| `sampleRate` (8000–96000) | The output rate is independent of the samples' own rate: the playback step resamples (3.5). The range covers everything from telephone quality to high-resolution audio. Lower rates give smaller files. |
| `channels` (`ChannelLayout`) | Stereo is what the piano was recorded in. Mono is offered for small files and simple players. It's an enum rather than an `int` because only two layouts make sense, so invalid counts can't be expressed. |
| `masterGainDb` (−60 to +12) | One overall volume control, in decibels because that's how loudness is perceived and specified. Above +12 dB everything would just hit the limiter, and below −60 dB is effectively silence. |
| `tailMs` (0–10000) | How long to keep rendering after the last note's release, so that releases (and Phase 3 reverb) can ring out. It's an **explicit length** rather than "render until silent", so the file length is predictable and testable (P3, P6). The default of 1000 ms covers the piano's 0.8 s release. |

`OutputFormat.DEFAULT` (44100, stereo, 0 dB, 1000 ms) is a named constant, so the defaults are written down in one place.

**`ChannelLayout { MONO(1), STEREO(2) }`**: `count()` gives the number of interleaved channels that buffers and encoders need.

**`Interpolation { LINEAR }`**: the quality versus speed choice for reading between samples. It's an enum so that Phase 2 can add `NEAREST`, `CUBIC` and `SINC` without changing any signature.

**`SynthOptions(PresetRef preset, OutputFormat output, Interpolation interpolation)`**: all the sound options, **grouped by concern**.

- *Why groups instead of flat fields:* each group is validated as a unit (an `OutputFormat` is always internally consistent), and a caller overrides a whole concern at once. The flat alternative would have about 25 fields by Phase 3.
- *Why `null` becomes a default in the constructor:* Phase 1 groups always need a value. Filling defaults at construction means the engine never checks for `null`.
- *Why `defaults()` plus `with…` methods:* adding a group in Phase 2 or 3 changes the record's canonical constructor. Callers who only use `defaults()` and `with…` keep compiling.
- *The Phase 2/3 contrast:* override groups such as `EnvelopeOverride` stay `null` when unused, because there `null` means "keep the SoundFont's own value" rather than "use an engine default".

**`SynthRequest(List<NoteSpec> notes, SynthOptions options)`**: one render job.

| Field | Why |
|---|---|
| `notes` (non-empty, copied, no `null`s) | The buffer length is defined by the last note's release, so an empty request has no meaningful length. The list is copied so a caller changing their list afterwards can't affect a render that may be running on another thread (P4). |
| `options` (`null` → defaults) | So that the simplest call is `new SynthRequest(notes, null)`. |

The Phase 3 sustain pedal goes on the request, not in the options, because pedal presses are **timeline events** like notes, not settings.

**`SynthOutput(float[] samples, int sampleRate, int channels)`**: the result.

| Field | Why |
|---|---|
| `samples` | **Float** in −1…1, which is the natural format for mixing and further processing. The encoder decides the final bit depth (16-bit for WAV). **Interleaved** (L, R, L, R, …) because that's what WAV and most encoders want. The array isn't copied, for speed. That's safe because nobody keeps a reference to it after `render` returns. |
| `sampleRate`, `channels` | PCM is meaningless without them, so they always travel with the data. |

### 9.2 SoundFont model (`synth.soundfont`): what the file says

These classes are a faithful, immutable, in-memory picture of the SF2 file. They interpret nothing. All interpretation happens in the engine.

**`GeneratorType`**: one constant per generator the engine reads.

| Field | Why |
|---|---|
| `id` | The SF2 operator number, used to recognise the generator while parsing. Ids that aren't listed are **skipped rather than rejected**, so SoundFonts that use features we don't support (LFOs, the modulation envelope) still load (3.7). |
| `defaultValue` | The SF2 2.04 default, used when no zone sets the generator. For example −12000 timecents (≈1 ms) for envelope times, and 13500 cents (filter open) for the cutoff. |
| `min`, `max` | The SF2 legal range. Clamping happens **after** instrument and preset values are added, because the sum can leave the range even when each part is valid. Addresses, indices and ranges have no clamp. |
| `presetAllowed` | SF2 forbids some generators at preset level (sample addresses, `keynum`, `velocity`, `sampleModes`, `exclusiveClass`, `overridingRootKey`). The resolver must ignore them there. |
| `unsignedAmount()` | Index and range generators store unsigned 16-bit values. Reading them as signed would turn sample 40000 into a negative number. |

**`Zone(EnumMap<GeneratorType, Integer>)`**: the generators set on one zone.

- *Why only the generators that are set:* resolution needs the chain "local zone → global zone → SF2 default" (3.4), which requires knowing whether a value was **set**. An earlier draft stored a `short[]` pre-filled with defaults. That design cannot tell "set to the default" from "not set", so it would let a global zone's value be wrongly overridden by a default. The `EnumMap` design replaced it.
- *Why `get` falls back to the default:* most callers want "the effective value" and shouldn't repeat the fallback. Callers that care use `has`.
- *Why ranges stay packed:* SF2 stores a key or velocity range as two bytes in one amount. Keeping that form means parsing doesn't special-case it. `rangeLow`/`rangeHigh` decode it where it's used.

**`SampleHeader(name, start, end, loopStart, loopEnd, sampleRate, originalPitch, pitchCorrection, sampleType)`**: mirrors the SF2 `shdr` record.

| Field | Why |
|---|---|
| `name` | Error messages and debugging only. |
| `start`, `end` | Where the audio sits in the `smpl` chunk, as **sample indices, not bytes**. That makes them directly usable with the `ShortBuffer`. |
| `loopStart`, `loopEnd` | Loop points for sustained instruments. They are unused by our piano, which has no loops, but required for other SoundFonts. |
| `sampleRate` | The rate the sample was recorded at, which can differ per sample. It's needed to compute the playback step. |
| `originalPitch` | The MIDI key the recording sounds at (the root key). Pitch shifting is relative to it. |
| `pitchCorrection` | Cents to fix a slightly out-of-tune recording. |
| `sampleType` | Mono, left, right, linked or ROM. Only the ROM flag is acted on (ROM samples have no data in the file). Left and right need no special handling, because their zones' pans already place them. The SF2 `sampleLink` field isn't kept for the same reason. |

**`InstrumentZone(Zone zone, SampleHeader sample)`** and **`PresetZone(Zone zone, Instrument instrument)`**: a zone plus its target, as a **direct reference resolved at load time**, not an index. The engine never does index lookups, and a broken index is caught once at startup (`SoundFontFormatException`) instead of on some later request.

**`Instrument(name, Zone globalZone, List<InstrumentZone> zones)`** and **`Preset(name, bank, program, Zone globalZone, List<PresetZone> zones)`**:

- `globalZone` is never `null`: a missing global zone becomes `Zone.EMPTY`, so the resolver needs no `null` checks.
- `zones` is an immutable copy, so the model can be shared safely between threads (P4).
- `bank` and `program` on `Preset` are its address, matched against `PresetRef`.

**`SoundFont(List<Preset> presets, ShortBuffer sampleData)`**:

| Field | Why |
|---|---|
| `presets` | The whole preset tree. It's small (45 KB for our piano), so it's parsed onto the heap. `findPreset` is a linear search, which is fine for a handful of presets. |
| `sampleData` | All sample audio as 16-bit values, **memory-mapped**, not copied. The 266 MB stays on disk until the OS pages in the parts actually played. It's a `ShortBuffer` because SF2 samples are 16-bit little-endian. It's read only with absolute `get(int)`, never with relative reads that move the buffer's shared position. That rule is what lets concurrent renders share it (P4). |

**`SoundFontLoader`** is stateless and has one static method. **`SoundFontFormatException`** is unchecked, because a bad SoundFont is a startup configuration error, not something callers can recover from.

### 9.3 Engine (`synth.engine`): how the sound is made

**`EnvelopeTimecents(int delay, int attack, int hold, int decay, int sustainCb, int release)`**: the volume envelope in SF2 units.

- *Why it isn't frames yet:* frames depend on the output sample rate, which `ZoneResolver` doesn't know (P2).
- *Why hold and decay already include keynum scaling:* the scaling depends on the key, which the resolver knows. Doing it there means the envelope doesn't need to know the key.

**`VoiceSpec(sample, start, end, loopStart, loopEnd, sampleModes, key, velocity, pitchCents, attenuationCb, filterCutoffCents, filterResonanceCb, pan, envelope)`**: everything one voice needs, fully resolved.

| Field(s) | Why |
|---|---|
| `sample` | For its own sample rate, which the playback step needs. |
| `start`, `end`, `loopStart`, `loopEnd` | Final absolute addresses **after** the zone's address offsets and clamping, so `Voice` never revisits generators. |
| `sampleModes` | The loop behaviour (0/2 none, 1 continuous, 3 until release). It's masked to two bits because SF2 defines no other values. |
| `key`, `velocity` | The **effective** values after the `keynum`/`velocity` generators, which can override the played note. They're kept for diagnostics and tests. |
| `pitchCents` | The **total** pitch offset (key vs root, scale tuning, coarse, fine and correction) as one number. `Voice` then does a single `pow` to get the step. |
| `attenuationCb` | The SoundFont's attenuation plus the velocity curve, combined into one number. `Voice` turns it into one gain multiplier. |
| `filterCutoffCents`, `filterResonanceCb`, `pan` | Kept in SF2 units. The filter needs the output rate to compute its coefficients (P2). |
| `envelope` | See above. |

`VoiceSpec` is **pure data**. That split lets `ZoneResolverTest` check all the SF2 rules without rendering any audio, and `VoiceTest` check rendering without building SoundFonts.

**`ZoneResolver`** is stateless and holds no fields. Its private `Generators` record holds the four zones that affect one voice: the preset's local and global zones, and the instrument's local and global zones. It answers `value(type)` with the SF2 rule "instrument absolute, preset additive, clamp".

**`VolumeEnvelope`**: the volume envelope, advanced one frame at a time.

| Field | Why |
|---|---|
| `delayFrames`, `attackFrames`, `holdFrames` | These stages have fixed lengths, so they're integer frame counts. |
| `decayCbPerFrame`, `releaseCbPerFrame` | **Rates, not lengths.** Decay and release are linear in centibels, and the release can start from any level (even mid-attack). A rate gives the right timing from any starting point. SF2 defines the times as "per 1000 cB", which is why the conversion divides by 1000. |
| `sustainCb` | The level the decay stops at, clamped to 960 (silence). |
| `stage`, `stageFrame`, `attenuationCb` | The current state. `attenuationCb` is shared by decay, sustain and release, because all three are expressed in cB. |

The main constructor takes frames rather than timecents, so tests can check exact frame-by-frame behaviour at simple numbers. `fromTimecents` does the unit conversion.

**`LowPassFilter`**: a resonant 2-pole low-pass filter.

| Field | Why |
|---|---|
| `b0`, `b1`, `b2`, `a1`, `a2` | The biquad coefficients, computed **once** per voice, because the cutoff doesn't change during a note in Phase 1. |
| `x1`, `x2`, `y1`, `y2` | The filter's memory of the last two inputs and outputs. This is why **every voice owns its own filter**: sharing one between voices would mix their histories. |
| `bypass` | True at the SF2 default cutoff ("filter off") or near Nyquist. Skipping the filter saves work and guarantees an unfiltered sample is truly untouched. |

**`SampleSource`** and **`Interpolator`**: `Interpolator` needs "the sample at index *i*" without knowing about loops or bounds. `Voice` supplies that view through `SampleSource`. That keeps interpolation testable with a plain array, and lets Phase 2's cubic and sinc modes reuse the same looping logic.

**`Voice`**: one playing sample.

| Field(s) | Why |
|---|---|
| `data` | The shared, memory-mapped sample buffer (read only, absolute reads). |
| `start`, `end`, `loopStart`, `loopEnd`, `sampleModes`, `loopValid` | Copied from the `VoiceSpec`, plus a precomputed "is the loop usable" flag (`loopEnd > loopStart`), so a broken loop simply plays unlooped. |
| `step` | Samples to advance per output frame. It's computed once (P1) and combines pitch and the sample-rate ratio. |
| `gain` | The amplitude multiplier from `attenuationCb`, computed once. |
| `leftGain`, `rightGain` | The constant-power pan gains, computed once. |
| `interpolation`, `filter`, `envelope` | This voice's own processing chain. The filter and envelope keep state, so they're per voice. |
| `startFrame`, `releaseFrame` | The note's onset and release, already in frames, because `Mixer` knows the output rate. |
| `position` | The read position in the sample, a `double`, because pitch-shifted playback lands between samples. |
| `released`, `ended` | State flags: whether the release has started, and whether the voice is finished (end of sample, or the envelope reached silence). |

**`Mixer`**: turns a request into output.

| Field / constant | Why |
|---|---|
| `soundFont` | The only field. `Mixer` keeps no state between renders: buffers and voices are local to each `render` call, which is why it's thread-safe without locks (P4). |
| `MAX_DURATION_MS = 600000` | A guard: without it, one bad onset (for example a note at hour 5) would allocate gigabytes. Ten minutes is far beyond any exercise. |
| `FADE_OUT_MS = 5` | Long enough to avoid a click when a still-sounding voice is cut at the buffer end, and short enough to be inaudible. |
| `LIMITER_THRESHOLD = 0.8` | Below it, audio passes **unchanged**, so normal levels (piano peaks are about 0.23 to 0.47) are bit-exact. Above it, a smooth curve prevents hard clipping when many loud notes overlap. |

**`Synthesizer`** has one field, the `Mixer`. It's a facade: the rest of the app sees one class with one method, and the engine's classes stay package-private, free to change.

**`Units`** has no fields: pure conversion functions, kept in one place so every class uses the same formulas.

### 9.4 Application layer (`application.sound`): what the app asks for

**`SoundNote(Pitch pitch, long onsetMs, long durationMs, int velocity)`**: the application's note.

- `pitch` is the domain's `Pitch`, because the application speaks in musical terms. The adapter converts it to a MIDI key.
- `onsetMs`, `durationMs` and `velocity` mean the same as in `NoteSpec`, so the adapter's mapping is trivial.
- *Why not reuse `NoteSpec`:* the application may not import infrastructure types (P5).

**`PcmAudio(float[] samples, int sampleRate, int channels)`**: the same shape as `SynthOutput`, for the same reason as above. It's the boundary type between synthesizing and encoding. The array is passed through without copying.

**`EncodedAudio(byte[] data, String mimeType, String extension)`**: the encoder reports **what** it produced. The service and controllers never hard-code "wav" or "audio/wav", so adding an MP3 or OGG encoder later doesn't touch them.

**`AudioContent(byte[] data, String mimeType, String fileName)`**: what the controller sends. The old `fileSize` field was dropped, because it was always `data.length`.

**`ISoundSynthesizerPort.synthesize(List<SoundNote>, InstrumentType)`**: deliberately **narrow**. Today's use cases supply no sound options, so the port carries none. When per-request options arrive (the deferred ticket), the port grows in one deliberate step, and the synth API already accepts them.

**`IntervalNoteSequencer`** constants (`PREROLL_MS = 63`, `NOTE_MS = 188`, `STACKED_NOTE_MS = 376`, `GAP_MS = 21`, `VELOCITY = 90`):

- These are the old MIDI timings, rounded to whole milliseconds (table in section 4), so exercises sound the same as before.
- Velocity 90 is the old fixed MIDI velocity, so the loudness level is unchanged.
- The pre-roll and the doubled stacked length are kept from the old code; their original intent isn't documented. The likely reasons: a short lead-in silence means a player that starts a few milliseconds late doesn't cut off the first attack, and a held chord needs more time than a single note to be heard as one sound.

### 9.5 Infrastructure wiring

**`SamplerSynthesizerAdapter`**:

| Field / constant | Why |
|---|---|
| `synthesizer` | The engine, injected so tests can use a fixture SoundFont. |
| `PRESETS` (`EnumMap<InstrumentType, PresetRef>`) | The **only** place that knows which preset is which instrument. Adding an instrument means one map entry, plus the SoundFont to back it. |

**`WavAudioEncoder`** has no fields, only constants: the MIME type, the extension, and the WAV header layout (44 bytes, PCM format 1, 16 bits). Writing the header by hand removes the last `javax.sound` dependency, and is about 15 lines.

**`SynthesizerConfig`** builds two beans:
- `SoundFont`, from `soundfont.path`. It's loaded once at startup instead of on every request as before, and missing files fail fast.
- `Synthesizer`, wrapping it.

The temp-file copy handles running from a packaged jar, where a classpath resource isn't a file and so can't be memory-mapped.

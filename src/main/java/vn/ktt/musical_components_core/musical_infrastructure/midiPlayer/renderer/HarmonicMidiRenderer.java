package vn.ktt.musical_components_core.musical_infrastructure.midiPlayer.renderer;

import org.springframework.stereotype.Component;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HarmonicMidiRenderer implements MidiRenderer {

    private static final float SAMPLE_RATE_HZ = 44100.0f;
    private static final int CHANNELS = 1;
    private static final float DEFAULT_TEMPO_MICROSECONDS_PER_QUARTER = 500_000f;

    private static final double REFERENCE_FREQUENCY_HZ = 440.0;
    private static final int REFERENCE_MIDI_NOTE = 69;
    private static final double ATTACK_SECONDS = 0.005;
    private static final double RELEASE_SECONDS = 0.020;
    private static final double MASTER_GAIN = 0.35;

    private record RenderedNote(int midiNote, int velocity, int startSample, int endSample) {}

    private record NoteStart(long startTick, int velocity) {}

    @Override
    public PcmSamples render(Sequence sequence) {
        try {
            float secondsPerTick = tempoSecondsPerTick(sequence);
            List<RenderedNote> renderedNotes = collectNotes(sequence, secondsPerTick);
            if (renderedNotes.isEmpty()) {
                return new PcmSamples(new float[0], SAMPLE_RATE_HZ, CHANNELS);
            }

            int totalSamples = renderedNotes.stream()
                    .mapToInt(RenderedNote::endSample)
                    .max().orElse(0) + 1;
            float[] samples = new float[totalSamples];
            for (RenderedNote renderedNote : renderedNotes) {
                renderNote(renderedNote, samples);
            }
            return new PcmSamples(samples, SAMPLE_RATE_HZ, CHANNELS);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to render MIDI sequence", e);
        }
    }

    private List<RenderedNote> collectNotes(Sequence sequence, float secondsPerTick) {
        List<RenderedNote> renderedNotes = new ArrayList<>();
        for (Track track : sequence.getTracks()) {
            Map<Integer, NoteStart> soundingNotes = new HashMap<>();
            for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
                MidiEvent event = track.get(eventIndex);
                if (!(event.getMessage() instanceof ShortMessage message)) {
                    continue;
                }
                int midiNote = message.getData1();
                int velocity = message.getData2();
                boolean noteOn = message.getCommand() == ShortMessage.NOTE_ON && velocity > 0;
                boolean noteOff = message.getCommand() == ShortMessage.NOTE_OFF
                        || (message.getCommand() == ShortMessage.NOTE_ON && velocity == 0);
                if (noteOn) {
                    soundingNotes.put(midiNote, new NoteStart(event.getTick(), velocity));
                } else if (noteOff) {
                    NoteStart noteStart = soundingNotes.remove(midiNote);
                    if (noteStart != null) {
                        renderedNotes.add(toRenderedNote(noteStart, event.getTick(), midiNote, secondsPerTick));
                    }
                }
            }
        }
        return renderedNotes;
    }

    private RenderedNote toRenderedNote(NoteStart noteStart, long endTick, int midiNote, float secondsPerTick) {
        int startSample = (int) Math.round(noteStart.startTick() * secondsPerTick * SAMPLE_RATE_HZ);
        int endSample = (int) Math.round(endTick * secondsPerTick * SAMPLE_RATE_HZ);
        return new RenderedNote(midiNote, noteStart.velocity(), startSample, endSample);
    }

    private void renderNote(RenderedNote note, float[] samples) {
        double frequencyHz = REFERENCE_FREQUENCY_HZ
                * Math.pow(2.0, (note.midiNote() - REFERENCE_MIDI_NOTE) / 12.0);
        int startSample = note.startSample();
        int endSample = Math.min(note.endSample(), samples.length);
        int attackSamples = (int) (SAMPLE_RATE_HZ * ATTACK_SECONDS);
        int releaseSamples = (int) (SAMPLE_RATE_HZ * RELEASE_SECONDS);
        double velocityRatio = note.velocity() / 127.0;

        for (int sampleIndex = startSample; sampleIndex < endSample; sampleIndex++) {
            int elapsedSamples = sampleIndex - startSample;
            double timeSeconds = elapsedSamples / SAMPLE_RATE_HZ;
            double amplitude = envelope(elapsedSamples, endSample - startSample, attackSamples, releaseSamples);
            double waveform = Math.sin(2 * Math.PI * frequencyHz * timeSeconds)
                    + 0.5 * Math.sin(2 * Math.PI * 2 * frequencyHz * timeSeconds)
                    + 0.25 * Math.sin(2 * Math.PI * 3 * frequencyHz * timeSeconds);
            samples[sampleIndex] += (float) (waveform * amplitude * velocityRatio * MASTER_GAIN);
        }
    }

    private double envelope(int elapsedSamples, int noteDurationSamples, int attackSamples, int releaseSamples) {
        if (elapsedSamples < attackSamples) {
            return (double) elapsedSamples / attackSamples;
        }
        if (elapsedSamples > noteDurationSamples - releaseSamples) {
            return Math.max(0, (double) (noteDurationSamples - elapsedSamples) / releaseSamples);
        }
        return 1.0;
    }

    private float tempoSecondsPerTick(Sequence sequence) throws Exception {
        float tempoMicrosecondsPerQuarter = DEFAULT_TEMPO_MICROSECONDS_PER_QUARTER;
        Track track = sequence.getTracks()[0];
        for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
            MidiEvent event = track.get(eventIndex);
            if (event.getMessage() instanceof MetaMessage meta && meta.getType() == 0x51) {
                byte[] tempoBytes = meta.getData();
                tempoMicrosecondsPerQuarter = ((tempoBytes[0] & 0xff) << 16)
                        | ((tempoBytes[1] & 0xff) << 8)
                        | (tempoBytes[2] & 0xff);
            }
        }
        return tempoMicrosecondsPerQuarter / 1_000_000f / sequence.getResolution();
    }
}

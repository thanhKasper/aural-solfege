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
public class    HarmonicMidiRenderer implements MidiRenderer {

    private static final float SAMPLE_RATE = 44100.0f;
    private static final int CHANNELS = 1;
    private static final float DEFAULT_TEMPO_MPQ = 500_000f;

    private record RenderedNote(int note, int velocity, int startSample, int endSample) {}

    @Override
    public PcmSamples render(Sequence sequence) {
        try {
            float secondsPerTick = tempoSecondsPerTick(sequence);
            List<RenderedNote> notes = collectNotes(sequence, secondsPerTick);
            if (notes.isEmpty()) {
                return new PcmSamples(new float[0], SAMPLE_RATE, CHANNELS);
            }

            int length = notes.stream().mapToInt(RenderedNote::endSample).max().orElse(0) + 1;
            float[] samples = new float[length];
            for (RenderedNote note : notes) {
                renderNote(note, samples);
            }
            return new PcmSamples(samples, SAMPLE_RATE, CHANNELS);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to render MIDI sequence", e);
        }
    }

    private List<RenderedNote> collectNotes(Sequence sequence, float secondsPerTick) {
        List<RenderedNote> notes = new ArrayList<>();
        for (Track track : sequence.getTracks()) {
            Map<Integer, long[]> active = new HashMap<>();
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                if (!(event.getMessage() instanceof ShortMessage message)) {
                    continue;
                }
                int note = message.getData1();
                boolean noteOn = message.getCommand() == ShortMessage.NOTE_ON && message.getData2() > 0;
                boolean noteOff = message.getCommand() == ShortMessage.NOTE_OFF
                        || (message.getCommand() == ShortMessage.NOTE_ON && message.getData2() == 0);
                if (noteOn) {
                    active.put(note, new long[]{event.getTick(), message.getData2()});
                } else if (noteOff) {
                    long[] start = active.remove(note);
                    if (start != null) {
                        notes.add(toRenderedNote(start, event.getTick(), note, secondsPerTick));
                    }
                }
            }
        }
        return notes;
    }

    private RenderedNote toRenderedNote(long[] start, long endTick, int note, float secondsPerTick) {
        int startSample = (int) Math.round(start[0] * secondsPerTick * SAMPLE_RATE);
        int endSample = (int) Math.round(endTick * secondsPerTick * SAMPLE_RATE);
        return new RenderedNote(note, (int) start[1], startSample, endSample);
    }

    private void renderNote(RenderedNote note, float[] samples) {
        double frequency = 440.0 * Math.pow(2.0, (note.note() - 69) / 12.0);
        int start = note.startSample();
        int end = Math.min(note.endSample(), samples.length);
        int attack = (int) (SAMPLE_RATE * 0.005);
        int release = (int) (SAMPLE_RATE * 0.020);
        double velocity = note.velocity() / 127.0;

        for (int i = start; i < end; i++) {
            double t = (i - start) / SAMPLE_RATE;
            double envelope = envelope(i - start, end - start, attack, release);
            double wave = Math.sin(2 * Math.PI * frequency * t)
                    + 0.5 * Math.sin(2 * Math.PI * 2 * frequency * t)
                    + 0.25 * Math.sin(2 * Math.PI * 3 * frequency * t);
            samples[i] += (float) (wave * envelope * velocity * 0.35);
        }
    }

    private double envelope(int index, int length, int attack, int release) {
        if (index < attack) {
            return (double) index / attack;
        }
        if (index > length - release) {
            return Math.max(0, (double) (length - index) / release);
        }
        return 1.0;
    }

    private float tempoSecondsPerTick(Sequence sequence) throws Exception {
        float tempoMpq = DEFAULT_TEMPO_MPQ;
        Track track = sequence.getTracks()[0];
        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            if (event.getMessage() instanceof MetaMessage meta && meta.getType() == 0x51) {
                byte[] data = meta.getData();
                tempoMpq = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
            }
        }
        return tempoMpq / 1_000_000f / sequence.getResolution();
    }
}

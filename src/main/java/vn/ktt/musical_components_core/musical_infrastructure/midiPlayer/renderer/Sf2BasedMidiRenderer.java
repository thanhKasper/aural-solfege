package vn.ktt.musical_components_core.musical_infrastructure.midiPlayer.renderer;

import com.sun.media.sound.AudioSynthesizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Track;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Primary
public class Sf2BasedMidiRenderer implements MidiRenderer {

    private static final float SAMPLE_RATE = 44100.0f;
    private static final int BITS_PER_SAMPLE = 16;
    private static final int CHANNELS = 1;
    private static final float TAIL_SECONDS = 2.0f;

    private final ResourceLoader resourceLoader;
    private final String soundfontPath;

    public Sf2BasedMidiRenderer(ResourceLoader resourceLoader,
                                @Value("${soundfont.path:classpath:soundfonts/piano.sf2}") String soundfontPath) {
        this.resourceLoader = resourceLoader;
        this.soundfontPath = soundfontPath;
    }

    @Override
    public synchronized PcmSamples render(Sequence sequence) {
        try (AudioSynthesizer synthesizer = (AudioSynthesizer) MidiSystem.getSynthesizer()) {
            Soundbank soundbank = loadSoundbank();
            AudioFormat format = new AudioFormat(SAMPLE_RATE, BITS_PER_SAMPLE, CHANNELS, true, false);

            Map<String, Object> properties = new HashMap<>();
            properties.put("interpolation", "linear");

            AudioInputStream stream = synthesizer.openStream(format, properties);
            try (Receiver receiver = synthesizer.getReceiver()) {
                Soundbank defaultSoundbank = synthesizer.getDefaultSoundbank();
                if (defaultSoundbank != null) {
                    synthesizer.unloadAllInstruments(defaultSoundbank);
                }
                synthesizer.loadAllInstruments(soundbank);

                double durationSeconds = send(sequence, receiver);
                long frameLength = (long) (stream.getFormat().getFrameRate() * (durationSeconds + TAIL_SECONDS));
                stream = new AudioInputStream(stream, stream.getFormat(), frameLength);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                AudioSystem.write(stream, AudioFileFormat.Type.WAVE, out);
                byte[] pcm = out.toByteArray();

                int frameCount = Math.min((int) frameLength, pcm.length / 2);
                float[] samples = new float[frameCount];
                for (int i = 0; i < frameCount; i++) {
                    short value = (short) ((pcm[2 * i] & 0xff) | (pcm[2 * i + 1] << 8));
                    samples[i] = value / 32768.0f;
                }
                int fadeSamples = (int) (SAMPLE_RATE * 0.05);
                int fadeLimit = Math.min(fadeSamples, frameCount);
                for (int i = 0; i < fadeLimit; i++) {
                    samples[i] *= (float) i / fadeSamples;
                }
                log.debug("Rendered {} frames ({}s) from sequence with soundfont {}", frameCount, durationSeconds, soundfontPath);
                return new PcmSamples(samples, SAMPLE_RATE, CHANNELS);
            } finally {
                stream.close();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to render MIDI sequence with soundfont", e);
        }
    }

    private Soundbank loadSoundbank() throws Exception {
        Resource resource = resourceLoader.getResource(soundfontPath);
        try (InputStream in = resource.getInputStream()) {
            return MidiSystem.getSoundbank(in);
        }
    }

    private double send(Sequence sequence, Receiver receiver) throws InvalidMidiDataException {
        int resolution = sequence.getResolution();
        Track[] tracks = sequence.getTracks();
        int[] positions = new int[tracks.length];
        int tempoMicrosPerQuarter = 500_000;
        long lastTick = 0;
        long currentTimeMicros = 0;

        while (true) {
            MidiEvent nextEvent = null;
            int selectedTrack = -1;
            for (int i = 0; i < tracks.length; i++) {
                if (positions[i] < tracks[i].size()) {
                    MidiEvent event = tracks[i].get(positions[i]);
                    if (nextEvent == null || event.getTick() < nextEvent.getTick()) {
                        nextEvent = event;
                        selectedTrack = i;
                    }
                }
            }
            if (selectedTrack == -1) {
                break;
            }
            positions[selectedTrack]++;

            long tick = nextEvent.getTick();
            currentTimeMicros += ((tick - lastTick) * tempoMicrosPerQuarter) / resolution;
            lastTick = tick;

            MidiMessage message = nextEvent.getMessage();
            // Unnecessary to be removed for the next refactor
            if (message instanceof MetaMessage meta) {
                if (meta.getType() == 0x51) {
                    byte[] data = meta.getData();
                    tempoMicrosPerQuarter = ((data[0] & 0xff) << 16)
                            | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
                }
            } else {
                receiver.send(message, currentTimeMicros);
            }
        }
        return currentTimeMicros / 1_000_000.0;
    }
}

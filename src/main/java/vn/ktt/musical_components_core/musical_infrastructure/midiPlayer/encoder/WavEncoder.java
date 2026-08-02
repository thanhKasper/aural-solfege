package vn.ktt.musical_components_core.musical_infrastructure.midiPlayer.encoder;

import org.springframework.stereotype.Component;
import vn.ktt.musical_components_core.musical_infrastructure.midiPlayer.renderer.PcmSamples;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Component
public class WavEncoder {

    public byte[] encode(PcmSamples audio) {
        AudioFormat format = new AudioFormat(audio.sampleRate(), 16, audio.channels(), true, false);
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
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encode WAV", e);
        }
    }
}

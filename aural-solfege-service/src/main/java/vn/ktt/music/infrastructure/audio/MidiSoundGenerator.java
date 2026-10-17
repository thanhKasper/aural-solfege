package vn.ktt.music.infrastructure.audio;

import org.springframework.stereotype.Component;
import vn.ktt.music.application.sound.dto.IntervalRangeParameters;
import vn.ktt.music.application.sound.outbound.ISoundGeneratorPort;
import vn.ktt.music.domain.atom.Pitch;
import vn.ktt.music.domain.composition.Interval;
import vn.ktt.music.infrastructure.audio.encoder.WavEncoder;
import vn.ktt.music.infrastructure.audio.midi.MidiSequenceBuilder;
import vn.ktt.music.infrastructure.audio.renderer.IMidiRenderer;
import vn.ktt.music.infrastructure.audio.renderer.PcmSamples;

import javax.sound.midi.Sequence;

// @TODO: Not precise base on how the sound is processed, need more investigation to create a more completed audio file generation. Need an architecture redesign
@Component
public class MidiSoundGenerator implements ISoundGeneratorPort {

    private final MidiSequenceBuilder sequenceBuilder;
    private final IMidiRenderer renderer;
    private final WavEncoder encoder;

    public MidiSoundGenerator(MidiSequenceBuilder sequenceBuilder, IMidiRenderer renderer, WavEncoder encoder) {
        this.sequenceBuilder = sequenceBuilder;
        this.renderer = renderer;
        this.encoder = encoder;
    }

    @Override
    public byte[] createIntervalRangeSound(IntervalRangeParameters parameters) {
        Sequence sequence = sequenceBuilder.build(parameters);
        PcmSamples samples = renderer.render(sequence);
        return encoder.encode(samples);
    }

    @Override
    public byte[] createIntervalSound(Pitch startingPitch, Interval interval, Interval.Texture texture) {
        Sequence sequence = sequenceBuilder.buildInterval(startingPitch, interval, texture);
        PcmSamples samples = renderer.render(sequence);
        return encoder.encode(samples);
    }
}

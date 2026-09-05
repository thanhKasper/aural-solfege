package vn.ktt.musical_components_core.musical_infrastructure.midiPlayer;

import org.springframework.stereotype.Component;
import vn.ktt.musical_components_core.musical_application.sound_controller.dtos.IntervalRangeParameters;
import vn.ktt.musical_components_core.musical_application.sound_controller.outbound.SoundGeneratorPort;
import vn.ktt.musical_components_core.musical_domains.music_atom.Pitch;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Interval;
import vn.ktt.musical_components_core.musical_infrastructure.midiPlayer.encoder.WavEncoder;
import vn.ktt.musical_components_core.musical_infrastructure.midiPlayer.midi.MidiSequenceBuilder;
import vn.ktt.musical_components_core.musical_infrastructure.midiPlayer.renderer.MidiRenderer;
import vn.ktt.musical_components_core.musical_infrastructure.midiPlayer.renderer.PcmSamples;

import javax.sound.midi.Sequence;

// @TODO: Not precise base on how the sound is processed, need more investigation to create a more completed audio file generation.
@Component
public class MidiSoundGenerator implements SoundGeneratorPort {

    private final MidiSequenceBuilder sequenceBuilder;
    private final MidiRenderer renderer;
    private final WavEncoder encoder;

    public MidiSoundGenerator(MidiSequenceBuilder sequenceBuilder, MidiRenderer renderer, WavEncoder encoder) {
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

package vn.ktt.musical_components_core.musical_infrastructure.midiPlayer;

import vn.ktt.musical_components_core.musical_application.sound_controller.dtos.IntervalRangeParameters;
import vn.ktt.musical_components_core.musical_application.sound_controller.outbound.SoundGeneratorPort;

public class MidiSoundGenerator implements SoundGeneratorPort {
    @Override
    public byte[] createIntervalRange(IntervalRangeParameters parameters) {
        return new byte[0];
    }
}

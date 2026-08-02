package vn.ktt.musical_components_core.musical_infrastructure.midiPlayer.renderer;

import javax.sound.midi.Sequence;

public interface MidiRenderer {

    PcmSamples render(Sequence sequence);
}

package vn.ktt.music.infrastructure.audio.renderer;

import javax.sound.midi.Sequence;

public interface IMidiRenderer {

    PcmSamples render(Sequence sequence);
}

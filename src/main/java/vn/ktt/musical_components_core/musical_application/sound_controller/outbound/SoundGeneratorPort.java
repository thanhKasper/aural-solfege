package vn.ktt.musical_components_core.musical_application.sound_controller.outbound;

import vn.ktt.musical_components_core.musical_application.sound_controller.dtos.IntervalRangeParameters;
import vn.ktt.musical_components_core.musical_domains.music_atom.Pitch;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Interval;

public interface SoundGeneratorPort {
    byte[] createIntervalRangeSound(IntervalRangeParameters parameters);
    byte[] createIntervalSound(Pitch startingPitch, Interval interval, Interval.Texture texture);
}

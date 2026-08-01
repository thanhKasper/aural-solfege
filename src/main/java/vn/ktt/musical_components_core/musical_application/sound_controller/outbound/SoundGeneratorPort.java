package vn.ktt.musical_components_core.musical_application.sound_controller.outbound;

import vn.ktt.musical_components_core.musical_application.sound_controller.dtos.IntervalRangeParameters;

public interface SoundGeneratorPort {
    byte[] createIntervalRange(IntervalRangeParameters parameters);
}

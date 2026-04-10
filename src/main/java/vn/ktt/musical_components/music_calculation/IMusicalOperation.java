package vn.ktt.musical_components.music_calculation;

import vn.ktt.musical_components.music_elements.Pitch;

public interface IMusicalComputation {
    Pitch getPitchWithHalfStep(Pitch basePitch, int halfStepCount);
    Pitch getNextPitch(Pitch basePitch);
    Pitch getPreviousPitch(Pitch basePitch);
}

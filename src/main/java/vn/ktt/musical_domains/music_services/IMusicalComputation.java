package vn.ktt.musical_domains.music_services;

import vn.ktt.musical_domains.music_elements.Pitch;

public interface IMusicalComputation {
    Pitch getPitchWithHalfStep(Pitch basePitch, int halfStepCount);
    Pitch getNextPitch(Pitch basePitch);
    Pitch getPreviousPitch(Pitch basePitch);
}

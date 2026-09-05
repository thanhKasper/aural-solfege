package vn.ktt.musical_components_core.musical_domains.music_services;

import vn.ktt.musical_components_core.musical_domains.music_atom.Pitch;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Interval;

public class MusicalOperation implements IMusicalOperation {
    @Override
    public Pitch getUpperBoundPitchFromInterval(Pitch lowerBoundPitch, Interval.IntervalType intervalType) {
        return lowerBoundPitch.getPitchAfterHalfSteps(-intervalType.getHalfSteps());
    }
}

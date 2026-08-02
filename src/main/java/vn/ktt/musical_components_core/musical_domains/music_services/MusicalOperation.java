package vn.ktt.musical_components_core.musical_domains.music_services;

import vn.ktt.musical_components_core.musical_domains.music_atom.Pitch;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Interval;

public class MusicalOperation implements IMusicalOperation {
    public Pitch getLowestPitch() {
        return new Pitch(Pitch.Note.A, Pitch.Accidental.NONE, Pitch.Octave.ZEROTH);
    }

    public Pitch getHighestPitch() {
        return new Pitch(Pitch.Note.C, Pitch.Accidental.NONE, Pitch.Octave.EIGHT);
    }

    @Override
    public Pitch getHighestLowerBoundIntervalPitch(Interval.IntervalType intervalType) {
        return getHighestPitch().getPitchAfterHalfSteps(-intervalType.getHalfSteps());
    }

    @Override
    public Pitch getLowestUpperBoundIntervalPitch(Interval.IntervalType intervalType) {
        return getLowestPitch().getPitchAfterHalfSteps(intervalType.getHalfSteps());
    }
}

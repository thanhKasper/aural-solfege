package vn.ktt.musical_components_core.musical_domains.music_services;

import vn.ktt.musical_components_core.musical_domains.music_atom.Pitch;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Interval;

import java.util.concurrent.ThreadLocalRandom;

public class MusicalOperation implements IMusicalOperation {
    @Override
    public Pitch getUpperBoundPitchFromInterval(Pitch lowerBoundPitch, Interval.IntervalType intervalType) {
        return lowerBoundPitch.getPitchAfterHalfSteps(intervalType.getHalfSteps());
    }

    @Override
    public Pitch getLowerBoundPitchFromInterval(Pitch upperBoundPitch, Interval.IntervalType intervalType) {
        return upperBoundPitch.getPitchAfterHalfSteps(-intervalType.getHalfSteps());
    }

    @Override
    public Pitch getRandomPitch(Pitch lowerBoundPitch, Pitch upperBoundPitch) {
        var randomMidiNumber = ThreadLocalRandom.current().nextInt(lowerBoundPitch.toMidiNumber(), upperBoundPitch.toMidiNumber() + 1);
        return Pitch.convertFromMidiNumber(randomMidiNumber);
    }
}

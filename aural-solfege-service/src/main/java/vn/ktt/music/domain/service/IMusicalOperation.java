package vn.ktt.music.domain.service;

import vn.ktt.music.domain.atom.Pitch;
import vn.ktt.music.domain.composition.Interval.*;

public interface IMusicalOperation {
    Pitch getUpperBoundPitchFromInterval(Pitch lowerBoundPitch, IntervalType intervalType);
    Pitch getLowerBoundPitchFromInterval(Pitch upperBoundPitch, IntervalType intervalType);
    Pitch getRandomPitch(Pitch lowerBoundPitch, Pitch upperBoundPitch);
}

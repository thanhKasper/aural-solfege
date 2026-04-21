package vn.ktt.musical_components_core.musical_domains.music_services;

import vn.ktt.musical_components_core.musical_domains.music_atom.Pitch;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Interval.*;

public interface IMusicalOperation {
    Pitch getLowestPitch();
    Pitch getHighestPitch();
    Pitch getHighestLowerBoundIntervalPitch(IntervalNumber intervalNumber);
}

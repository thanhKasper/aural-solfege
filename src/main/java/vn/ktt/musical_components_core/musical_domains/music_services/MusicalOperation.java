package vn.ktt.musical_components_core.musical_domains.music_services;

import vn.ktt.musical_components_core.musical_domains.instruments.Instrument;
import vn.ktt.musical_components_core.musical_domains.music_atom.Pitch;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Interval;

public class MusicalOperation implements IMusicalOperation {
    public Pitch getLowestPitch(Instrument instrument) {
        return instrument.getLowestPitch();
    }

    public Pitch getHighestPitch(Instrument instrument) {
        return instrument.getHighestPitch();
    }

    @Override
    public Pitch getHighestLowerBoundIntervalPitch(Instrument instrument, Interval.IntervalType intervalType) {
        return getHighestPitch(instrument).getPitchAfterHalfSteps(-intervalType.getHalfSteps());
    }

    @Override
    public Pitch getLowestUpperBoundIntervalPitch(Instrument instrument, Interval.IntervalType intervalType) {
        return getLowestPitch(instrument).getPitchAfterHalfSteps(intervalType.getHalfSteps());
    }
}

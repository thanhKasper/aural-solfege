package vn.ktt.musical_components_core.musical_domains.instruments;

import vn.ktt.musical_components_core.musical_domains.music_atom.Pitch;

public interface Instrument {
    InstrumentType getInstrumentType();
    Pitch getLowestPitch();
    Pitch getHighestPitch();
}

package vn.ktt.musical_components_core.musical_application.sound_configuration.outbound;

import vn.ktt.musical_components_core.musical_domains.instruments.Instrument;

public interface InstrumentConfigurationPort {
    Instrument getActiveInstrument();
}

package vn.ktt.music.application.instrument.outbound;

import vn.ktt.music.domain.instrument.Instrument;

public interface IInstrumentConfigurationPort {
    Instrument getActiveInstrument();
}

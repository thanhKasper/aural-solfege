package vn.ktt.musical_components_core.musical_application.instrument_management.inbound;

public interface ISoundConfiguration {
    void changeInstrument(MusicalInstrument musicalInstrument);

    enum MusicalInstrument {
        PIANO,
        GUITAR,
        VIOLIN,
    }
}

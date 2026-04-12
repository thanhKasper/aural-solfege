package vn.ktt.musical_components_core.musical_application.sound_controller.inbound;

public interface ISoundConfiguration {
    void changeInstrument(MusicalInstrument musicalInstrument);

    enum MusicalInstrument {
        PIANO,
        GUITAR,
        VIOLIN,
    }
}

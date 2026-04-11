package vn.ktt.sound_controller;

public interface ISoundConfiguration {
    void changeInstrument(Instrument instrument);

    enum Instrument {
        PIANO,
        GUITAR,
        VIOLIN,
    }
}

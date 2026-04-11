package vn.ktt.shared.sound_player;

public interface ISoundConfiguration {
    void changeInstrument(Instrument instrument);
    void addSoundFont();
    void selectSoundFontDefault();

    enum Instrument {
        PIANO,
        GUITAR,
        VIOLIN,
    }
}

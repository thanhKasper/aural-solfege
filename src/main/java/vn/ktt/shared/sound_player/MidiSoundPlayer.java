package vn.ktt.shared.sound_player;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

public class MidiSoundPlayer implements ISoundPlayer, ISoundConfiguration {
    private final Synthesizer synthesizer;

    public MidiSoundPlayer() throws MidiUnavailableException {
        synthesizer = MidiSystem.getSynthesizer();
        synthesizer.open();
    }

    @Override
    public void play() {

    }

    @Override
    public void changeInstrument(Instrument instrument) {

    }

    @Override
    public void addSoundFont() {

    }

    @Override
    public void selectSoundFontDefault() {

    }
}

package vn.ktt;

import vn.ktt.musical_domains.music_elements.Pitch;
import vn.ktt.sound_controller.MidiSoundPlayer;

public class Main {
    static void main() {
        var soundPlayer = new MidiSoundPlayer();
        var cMiddle = new Pitch(Pitch.Note.C, Pitch.Accidental.NONE, Pitch.Octave.FOURTH);
    }
}

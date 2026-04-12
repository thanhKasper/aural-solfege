package vn.ktt;

import vn.ktt.musical_domains.music_factory.MusicalEntityFactory;
import vn.ktt.musical_infrastructure.MidiSoundPlayer;

public class Main {
    static void main() {
        var musicFactory = new MusicalEntityFactory();
        var soundPlayer = new MidiSoundPlayer(musicFactory);
        soundPlayer.playPitch("D4");
        soundPlayer.playPitch("A4");
        soundPlayer.playPitch("A4");
        soundPlayer.playPitch("A4");
        soundPlayer.playPitch("A4");
        soundPlayer.playPitch("G4");
        soundPlayer.playPitch("F#4");
        soundPlayer.playPitch("G4");
        soundPlayer.playPitch("F#4");
        soundPlayer.playPitch("D4");
        soundPlayer.playPitch("D4");
    }
}

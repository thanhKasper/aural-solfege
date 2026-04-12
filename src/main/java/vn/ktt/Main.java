package vn.ktt;

import vn.ktt.musical_domains.music_factory.MusicalEntityFactory;
import vn.ktt.musical_infrastructure.MidiSoundPlayer;

public class Main {
    static void main() {
        var soundPlayer = new MidiSoundPlayer();
        var musicFactory = new MusicalEntityFactory();
        soundPlayer.playPitch(musicFactory.getPitch("C4"));
        soundPlayer.playPitch(musicFactory.getPitch("D4"));
        soundPlayer.playPitch(musicFactory.getPitch("E4"));
    }
}

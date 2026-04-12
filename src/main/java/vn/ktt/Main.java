package vn.ktt;

import vn.ktt.musical_components_core.musical_application.sound_controller.MusicalElementsSoundHandler;
import vn.ktt.musical_components_core.musical_application.sound_controller.inbound.IMusicalElementsSoundHandler;
import vn.ktt.musical_components_core.musical_domains.music_factory.MusicalEntityFactory;
import vn.ktt.musical_components_core.musical_infrastructure.MidiSoundPlayer;

public class Main {
    static void main() {
        var musicFactory = new MusicalEntityFactory();
        var soundPlayer = new MidiSoundPlayer(musicFactory);
        IMusicalElementsSoundHandler soundElementHandler = new MusicalElementsSoundHandler(soundPlayer, musicFactory);
        soundElementHandler.playStackedInterval("P5", "D3");
        soundElementHandler.playStackedInterval("P5", "D3");
        soundElementHandler.playBrokenInterval("P5", "D3", true);
    }
}

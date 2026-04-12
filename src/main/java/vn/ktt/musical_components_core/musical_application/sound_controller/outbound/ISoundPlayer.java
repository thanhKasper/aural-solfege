package vn.ktt.musical_components_core.musical_application.sound_controller.outbound;

import java.util.List;

public interface ISoundPlayer {
    void playSingleSound(String pitch);
    void playStackedSound(List<String> pitch);
}

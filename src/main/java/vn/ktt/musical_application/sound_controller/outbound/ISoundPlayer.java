package vn.ktt.musical_application.sound_controller.outbound;

import vn.ktt.musical_domains.music_elements.Pitch;

public interface ISoundPlayer {
    void playPitch(Pitch pitch);
}

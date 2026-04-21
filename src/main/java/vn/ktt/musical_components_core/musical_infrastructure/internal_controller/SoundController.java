package vn.ktt.musical_components_core.musical_infrastructure.internal_controller;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import vn.ktt.musical_components_core.musical_application.sound_controller.inbound.IMusicalElementsSoundHandler;

@Component
public class SoundController {
    private final IMusicalElementsSoundHandler soundHandler;

    public SoundController(IMusicalElementsSoundHandler soundHandler) {
        this.soundHandler = soundHandler;
    }

    public void playInterval(String interval, String soundTexture) {
        if (soundTexture.equals("ASCENDING")) {
            soundHandler.playAscendingSequentially(interval);
        }
        else if (soundTexture.equals("DESCENDING")) {
            soundHandler.playDescendingSequentially(interval);
        }
        else {
            throw new IllegalArgumentException("Unknown sound texture, the value must be ASCENDING or DESCENDING");
        }
    }
}

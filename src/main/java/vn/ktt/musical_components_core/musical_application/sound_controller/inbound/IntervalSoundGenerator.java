package vn.ktt.musical_components_core.musical_application.sound_controller.inbound;

import vn.ktt.musical_components_core.musical_application.sound_controller.dtos.AudioContent;

public interface IntervalSoundGenerator {
    AudioContent generateUpwardInterval(String interval, String texture);
    AudioContent generateDownwardInterval(String interval, String texture);
    AudioContent generateInterval(String interval, String texture);
}

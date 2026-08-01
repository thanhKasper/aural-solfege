package vn.ktt.musical_components_core.musical_application.sound_controller.inbound;

import vn.ktt.musical_components_core.musical_application.sound_controller.dtos.AudioContent;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Interval;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Texture;

public interface IntervalSoundGenerator {
    AudioContent generateUpwardInterval(Interval interval, Texture texture);
    AudioContent generateDownwardInterval(Interval interval, Texture texture);
    AudioContent generateInterval(Interval interval, Texture texture);
}

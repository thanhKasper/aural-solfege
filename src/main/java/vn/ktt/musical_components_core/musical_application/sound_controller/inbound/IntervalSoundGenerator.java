package vn.ktt.musical_components_core.musical_application.sound_controller.inbound;

import vn.ktt.musical_components_core.musical_application.sound_controller.dtos.AudioContent;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Interval;

public interface IntervalSoundGenerator {
    AudioContent generateUpwardInterval(Interval interval, Interval.Texture texture);
    AudioContent generateDownwardInterval(Interval interval, Interval.Texture texture);
    AudioContent generateInterval(Interval interval, Interval.Texture texture);
}

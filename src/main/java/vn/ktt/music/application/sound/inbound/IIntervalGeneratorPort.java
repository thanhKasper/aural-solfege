package vn.ktt.music.application.sound.inbound;

import vn.ktt.music.application.sound.dto.AudioContent;
import vn.ktt.music.domain.composition.Interval;

public interface IIntervalGeneratorPort {
    AudioContent generateUpwardInterval(Interval interval, Interval.Texture texture);
    AudioContent generateDownwardInterval(Interval interval, Interval.Texture texture);
    AudioContent generateInterval(Interval interval, Interval.Texture texture);
}

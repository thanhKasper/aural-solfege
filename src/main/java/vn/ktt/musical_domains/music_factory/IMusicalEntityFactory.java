package vn.ktt.musical_domains.music_factory;

import vn.ktt.musical_domains.music_compositions.Interval;
import vn.ktt.musical_domains.music_elements.Pitch;

public interface IMusicalEntityFactory {
    Pitch getPitch(String pitchNotation);
    Interval getInterval(String intervalNotation);
}

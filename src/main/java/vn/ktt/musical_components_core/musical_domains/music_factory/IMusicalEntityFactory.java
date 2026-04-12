package vn.ktt.musical_components_core.musical_domains.music_factory;

import vn.ktt.musical_components_core.musical_domains.music_compositions.Interval;
import vn.ktt.musical_components_core.musical_domains.music_atom.Pitch;

public interface IMusicalEntityFactory {
    Pitch getPitch(String pitchNotation);
    Interval getInterval(String intervalNotation);
}

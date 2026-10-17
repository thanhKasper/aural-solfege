package vn.ktt.music.domain.factory;

import vn.ktt.music.domain.composition.Interval;
import vn.ktt.music.domain.atom.Pitch;

public interface IMusicalEntityFactory {
    Pitch getPitch(String pitchNotation);
    Interval getInterval(String intervalNotation);

}

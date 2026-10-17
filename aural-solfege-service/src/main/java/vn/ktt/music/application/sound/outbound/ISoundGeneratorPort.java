package vn.ktt.music.application.sound.outbound;

import vn.ktt.music.application.sound.dto.IntervalRangeParameters;
import vn.ktt.music.domain.atom.Pitch;
import vn.ktt.music.domain.composition.Interval;

public interface ISoundGeneratorPort {
    byte[] createIntervalRangeSound(IntervalRangeParameters parameters);
    byte[] createIntervalSound(Pitch startingPitch, Interval interval, Interval.Texture texture);
}

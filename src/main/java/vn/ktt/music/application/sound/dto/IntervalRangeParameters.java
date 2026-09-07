package vn.ktt.music.application.sound.dto;

import lombok.Getter;
import lombok.Setter;
import vn.ktt.music.domain.atom.Pitch;
import vn.ktt.music.domain.composition.Interval;

@Getter
@Setter
public class IntervalRangeParameters {
    private Pitch lowestPitch;
    private Pitch highestPitch;
    private Interval interval;
    private Interval.Texture intervalTexture;
    private boolean reverse;
}

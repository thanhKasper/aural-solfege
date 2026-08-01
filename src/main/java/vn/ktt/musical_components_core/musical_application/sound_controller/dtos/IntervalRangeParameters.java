package vn.ktt.musical_components_core.musical_application.sound_controller.dtos;

import lombok.Getter;
import lombok.Setter;
import vn.ktt.musical_components_core.musical_domains.music_atom.Pitch;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Interval;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Texture;

@Getter
@Setter
public class IntervalRangeParameters {
    private Pitch lowestPitch;
    private Pitch highestPitch;
    private Interval interval;
    private Texture intervalTexture;
    private boolean reverse;
}

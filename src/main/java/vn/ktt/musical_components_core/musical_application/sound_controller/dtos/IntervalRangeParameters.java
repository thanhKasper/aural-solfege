package vn.ktt.musical_components_core.musical_application.sound_controller.dtos;

import lombok.Getter;
import lombok.Setter;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.musical_components_core.musical_domains.music_atom.Pitch;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Interval;

@Getter
@Setter
public class IntervalRangeParameters {
    private Pitch lowestPitch;
    private Pitch highestPitch;
    private Interval interval;
    private IntervalTexture intervalTexture;
    private boolean reverse;
}

package vn.ktt.ear_training_system.infrastructure.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import vn.ktt.ear_training_system.domain.interval_training.IntervalTexture;
import vn.ktt.ear_training_system.domain.interval_training.MusicalInterval;

public abstract class SingleIntervalExerciseActivityMixin {
    @JsonCreator
    public SingleIntervalExerciseActivityMixin(
            @JsonProperty("soundProperty") IntervalTexture soundProperty,
            @JsonProperty("interval") MusicalInterval interval,
            @JsonProperty("position") int position) {
    }
}

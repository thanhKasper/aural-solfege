package vn.ktt.ear_training_system.infrastructure.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;

public abstract class ListenIntervalContextMixin {
    @JsonCreator
    public ListenIntervalContextMixin(
            @JsonProperty("interval") MusicalInterval interval,
            @JsonProperty("direction") String direction,
            @JsonProperty("texture") IntervalTexture texture) {
    }
}

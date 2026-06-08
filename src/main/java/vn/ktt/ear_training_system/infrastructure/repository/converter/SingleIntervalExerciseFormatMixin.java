package vn.ktt.ear_training_system.infrastructure.repository.converter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import vn.ktt.ear_training_system.domain.interval_training.IntervalTexture;
import vn.ktt.ear_training_system.domain.interval_training.MusicalInterval;

public abstract class SingleIntervalExerciseFormatMixin {
    @JsonCreator
    public SingleIntervalExerciseFormatMixin(
            @JsonProperty("soundProperty") IntervalTexture soundProperty,
            @JsonProperty("interval") MusicalInterval interval,
            @JsonProperty("position") int position) {
    }
}

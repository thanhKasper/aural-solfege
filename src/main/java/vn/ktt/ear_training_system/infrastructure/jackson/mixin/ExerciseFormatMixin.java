package vn.ktt.ear_training_system.infrastructure.jackson.mixin;

import com.fasterxml.jackson.annotation.*;
import vn.ktt.ear_training_system.domain.interval_training.SingleIntervalExerciseFormat;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "formatType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SingleIntervalExerciseFormat.class, name = "single_interval")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ExerciseFormatMixin {
}

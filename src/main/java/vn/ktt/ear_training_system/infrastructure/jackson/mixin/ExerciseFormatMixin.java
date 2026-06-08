package vn.ktt.ear_training_system.infrastructure.jackson.mixin;

import com.fasterxml.jackson.annotation.*;
import vn.ktt.ear_training_system.domain.interval_training.SingleIntervalExerciseFormat;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "formatType")
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ExerciseFormatMixin {
}

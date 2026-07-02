package vn.ktt.ear_training_system.infrastructure.jackson.mixin.entity.exercise_activity;

import com.fasterxml.jackson.annotation.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ExerciseActivityMixin {
}

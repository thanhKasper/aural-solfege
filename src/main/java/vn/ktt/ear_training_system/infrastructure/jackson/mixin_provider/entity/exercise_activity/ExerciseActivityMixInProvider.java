package vn.ktt.ear_training_system.infrastructure.jackson.mixin_provider.entity.exercise_activity;

import vn.ktt.ear_training_system.domain.exercise.entity.ExerciseActivity;

public interface ExerciseActivityMixInProvider {
    Class<? extends ExerciseActivity> targetClass();
    Class<?> mixInClass();
    String typeName();
}

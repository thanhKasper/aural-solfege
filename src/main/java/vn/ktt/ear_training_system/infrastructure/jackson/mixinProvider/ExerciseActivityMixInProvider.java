package vn.ktt.ear_training_system.infrastructure.jackson.mixinProvider;

import vn.ktt.ear_training_system.domain.ExerciseActivity;

public interface ExerciseActivityMixInProvider {
    Class<? extends ExerciseActivity> targetClass();
    Class<?> mixInClass();
    String typeName();
}

package vn.ktt.ear_training_system.infrastructure.jackson.mixinProvider;

import vn.ktt.ear_training_system.domain.ExerciseFormat;

public interface ExerciseFormatMixInProvider {
    Class<? extends ExerciseFormat> targetClass();
    Class<?> mixInClass();
    String typeName();
}

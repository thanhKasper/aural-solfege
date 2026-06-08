package vn.ktt.ear_training_system.infrastructure.repository.converter;

import vn.ktt.ear_training_system.domain.ExerciseFormat;

public interface ExerciseFormatMixInProvider {
    Class<? extends ExerciseFormat> targetClass();
    Class<?> mixInClass();
}

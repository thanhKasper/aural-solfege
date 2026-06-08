package vn.ktt.ear_training_system.infrastructure.jackson.mixinProvider;

import vn.ktt.ear_training_system.application.dtos.ExerciseFormatDTO;

public interface ExerciseFormatDTOMixinProvider {
    Class<? extends ExerciseFormatDTO> targetClass();
    Class<?> mixInClass();
    String typeName();
}

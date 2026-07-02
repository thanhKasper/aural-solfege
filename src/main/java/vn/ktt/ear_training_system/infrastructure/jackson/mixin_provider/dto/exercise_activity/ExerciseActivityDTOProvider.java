package vn.ktt.ear_training_system.infrastructure.jackson.mixin_provider.dto.exercise_activity;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import vn.ktt.ear_training_system.application.dtos.ExerciseActivityDTO;

public interface ExerciseActivityDTOProvider {
    Class<? extends ExerciseActivityDTO> targetClass();
    String typeName();

    default NamedType toNamedType() {
        return new NamedType(targetClass(), typeName());
    }
}

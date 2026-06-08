package vn.ktt.ear_training_system.infrastructure.jackson.mixinProvider;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import vn.ktt.ear_training_system.application.dtos.ExerciseFormatDTO;

public interface ExerciseFormatDTOProvider {
    Class<? extends ExerciseFormatDTO> targetClass();
    String typeName();

    default NamedType toNamedType() {
        return new NamedType(targetClass(), typeName());
    }
}

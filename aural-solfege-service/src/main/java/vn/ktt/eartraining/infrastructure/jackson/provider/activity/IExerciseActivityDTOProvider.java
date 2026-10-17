package vn.ktt.eartraining.infrastructure.jackson.provider.activity;

import tools.jackson.databind.jsontype.NamedType;
import vn.ktt.eartraining.application.dto.activity.ExerciseActivityDTO;

public interface IExerciseActivityDTOProvider {
    Class<? extends ExerciseActivityDTO> targetClass();
    String typeName();

    default NamedType toNamedType() {
        return new NamedType(targetClass(), typeName());
    }
}

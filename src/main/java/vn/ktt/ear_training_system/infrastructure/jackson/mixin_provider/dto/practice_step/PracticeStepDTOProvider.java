package vn.ktt.ear_training_system.infrastructure.jackson.mixin_provider.dto.practice_step;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import vn.ktt.ear_training_system.application.dtos.PracticeStepDTO;

public interface PracticeStepDTOProvider {
    Class<? extends PracticeStepDTO> targetClass();
    String typeName();

    default NamedType toNamedType() {
        return new NamedType(targetClass(), typeName());
    }
}

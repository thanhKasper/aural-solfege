package vn.ktt.eartraining.infrastructure.jackson.provider.step;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import vn.ktt.eartraining.application.dto.step.PracticeStepDTO;

public interface IPracticeStepDTOProvider {
    Class<? extends PracticeStepDTO> targetClass();
    String typeName();

    default NamedType toNamedType() {
        return new NamedType(targetClass(), typeName());
    }
}

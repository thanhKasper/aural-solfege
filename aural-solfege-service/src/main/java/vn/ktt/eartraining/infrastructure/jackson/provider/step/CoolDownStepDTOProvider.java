package vn.ktt.eartraining.infrastructure.jackson.provider.step;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.application.dto.step.CoolDownStepDTO;
import vn.ktt.eartraining.application.dto.step.PracticeStepDTO;
import vn.ktt.eartraining.application.dto.step.PracticeStepType;

@Component
public class CoolDownStepDTOProvider implements IPracticeStepDTOProvider {
    @Override
    public Class<? extends PracticeStepDTO> targetClass() {
        return CoolDownStepDTO.class;
    }

    @Override
    public String typeName() {
        return PracticeStepType.COOL_DOWN.toString();
    }
}

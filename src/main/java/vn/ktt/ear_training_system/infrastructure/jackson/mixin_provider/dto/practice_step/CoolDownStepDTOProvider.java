package vn.ktt.ear_training_system.infrastructure.jackson.mixin_provider.dto.practice_step;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.practice_step.CoolDownStepDTO;
import vn.ktt.ear_training_system.application.dtos.practice_step.PracticeStepDTO;
import vn.ktt.ear_training_system.application.dtos.practice_step.PracticeStepType;

@Component
public class CoolDownStepDTOProvider implements PracticeStepDTOProvider {
    @Override
    public Class<? extends PracticeStepDTO> targetClass() {
        return CoolDownStepDTO.class;
    }

    @Override
    public String typeName() {
        return PracticeStepType.COOL_DOWN.toString();
    }
}

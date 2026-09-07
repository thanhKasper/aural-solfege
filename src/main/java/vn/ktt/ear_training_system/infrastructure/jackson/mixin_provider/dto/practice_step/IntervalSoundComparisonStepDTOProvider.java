package vn.ktt.ear_training_system.infrastructure.jackson.mixin_provider.dto.practice_step;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.practice_step.IntervalDistanceComparisonStepDTO;
import vn.ktt.ear_training_system.application.dtos.practice_step.PracticeStepDTO;
import vn.ktt.ear_training_system.application.dtos.practice_step.PracticeStepType;

@Component
public class IntervalSoundComparisonStepDTOProvider implements IPracticeStepDTOProvider {
    @Override
    public Class<? extends PracticeStepDTO> targetClass() {
        return IntervalDistanceComparisonStepDTO.class;
    }

    @Override
    public String typeName() {
        return PracticeStepType.INTERVAL_SOUND_COMPARISON.toString();
    }
}

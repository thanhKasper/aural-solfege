package vn.ktt.eartraining.infrastructure.jackson.provider.step;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.application.dto.step.IntervalDistanceComparisonStepDTO;
import vn.ktt.eartraining.application.dto.step.PracticeStepDTO;
import vn.ktt.eartraining.application.dto.step.PracticeStepType;

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

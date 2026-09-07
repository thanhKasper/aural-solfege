package vn.ktt.eartraining.infrastructure.jackson.provider.activity;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.application.dto.activity.ExerciseActivityDTO;
import vn.ktt.eartraining.application.dto.activity.ExerciseActivityType;
import vn.ktt.eartraining.application.dto.activity.IntervalSoundComparisonExerciseActivityDTO;

@Component
public class IntervalSoundComparisonExerciseActivityDTOProvider implements IExerciseActivityDTOProvider {
    @Override
    public Class<? extends ExerciseActivityDTO> targetClass() {
        return IntervalSoundComparisonExerciseActivityDTO.class;
    }

    @Override
    public String typeName() {
        return ExerciseActivityType.INTERVAL_SOUND_COMPARISON.name();
    }
}

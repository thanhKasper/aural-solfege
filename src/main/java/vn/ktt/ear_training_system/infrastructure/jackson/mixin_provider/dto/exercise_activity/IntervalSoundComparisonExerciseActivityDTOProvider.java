package vn.ktt.ear_training_system.infrastructure.jackson.mixin_provider.dto.exercise_activity;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.ExerciseActivityDTO;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.ExerciseActivityType;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.IntervalSoundComparisonExerciseActivityDTO;

@Component
public class IntervalSoundComparisonExerciseActivityDTOProvider implements ExerciseActivityDTOProvider {
    @Override
    public Class<? extends ExerciseActivityDTO> targetClass() {
        return IntervalSoundComparisonExerciseActivityDTO.class;
    }

    @Override
    public String typeName() {
        return ExerciseActivityType.INTERVAL_SOUND_COMPARISON.name();
    }
}

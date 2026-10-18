package vn.ktt.eartraining.infrastructure.jackson.provider.activity;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.application.dto.activity.ExerciseActivityDTO;
import vn.ktt.eartraining.application.dto.activity.ExerciseActivityType;
import vn.ktt.eartraining.application.dto.activity.SingleIntervalExerciseActivityDTO;

@Component
public class SingleIntervalExerciseActivityDTOProvider implements IExerciseActivityDTOProvider {
    @Override
    public Class<? extends ExerciseActivityDTO> targetClass() {
        return SingleIntervalExerciseActivityDTO.class;
    }

    @Override
    public String typeName() {
        return ExerciseActivityType.SINGLE_INTERVAL.name();
    }
}

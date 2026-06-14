package vn.ktt.ear_training_system.infrastructure.jackson.mixinProvider;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ExerciseActivityDTO;
import vn.ktt.ear_training_system.application.dtos.ExerciseActivityType;
import vn.ktt.ear_training_system.application.dtos.SingleIntervalExerciseActivityDTO;

@Component
public class SingleIntervalExerciseActivityDTOProvider implements ExerciseActivityDTOProvider {
    @Override
    public Class<? extends ExerciseActivityDTO> targetClass() {
        return SingleIntervalExerciseActivityDTO.class;
    }

    @Override
    public String typeName() {
        return ExerciseActivityType.SINGLE_INTERVAL.name();
    }
}

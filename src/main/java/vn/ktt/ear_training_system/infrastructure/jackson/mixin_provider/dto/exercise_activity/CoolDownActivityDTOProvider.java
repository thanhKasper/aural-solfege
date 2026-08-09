package vn.ktt.ear_training_system.infrastructure.jackson.mixin_provider.dto.exercise_activity;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.CoolDownRestActivityDTO;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.ExerciseActivityDTO;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.ExerciseActivityType;

@Component
public class CoolDownActivityDTOProvider implements ExerciseActivityDTOProvider {

    @Override
    public Class<? extends ExerciseActivityDTO> targetClass() {
        return CoolDownRestActivityDTO.class;
    }

    @Override
    public String typeName() {
        return ExerciseActivityType.COOL_DOWN.name();
    }
}

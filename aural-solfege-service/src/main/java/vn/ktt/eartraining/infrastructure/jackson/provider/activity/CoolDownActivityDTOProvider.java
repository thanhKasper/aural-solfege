package vn.ktt.eartraining.infrastructure.jackson.provider.activity;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.application.dto.activity.CoolDownRestActivityDTO;
import vn.ktt.eartraining.application.dto.activity.ExerciseActivityDTO;
import vn.ktt.eartraining.application.dto.activity.ExerciseActivityType;

@Component
public class CoolDownActivityDTOProvider implements IExerciseActivityDTOProvider {

    @Override
    public Class<? extends ExerciseActivityDTO> targetClass() {
        return CoolDownRestActivityDTO.class;
    }

    @Override
    public String typeName() {
        return ExerciseActivityType.COOL_DOWN.name();
    }
}

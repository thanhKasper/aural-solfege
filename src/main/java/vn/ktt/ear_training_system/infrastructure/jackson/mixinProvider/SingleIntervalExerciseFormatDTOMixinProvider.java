package vn.ktt.ear_training_system.infrastructure.jackson.mixinProvider;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ExerciseFormatDTO;
import vn.ktt.ear_training_system.application.dtos.ExerciseFormatType;
import vn.ktt.ear_training_system.application.dtos.SingleIntervalExerciseFormatDTO;
import vn.ktt.ear_training_system.infrastructure.jackson.mixin.ExerciseFormatDTOMixin;

@Component
public class SingleIntervalExerciseFormatDTOMixinProvider implements ExerciseFormatDTOMixinProvider {
    @Override
    public Class<? extends ExerciseFormatDTO> targetClass() {
        return SingleIntervalExerciseFormatDTO.class;
    }

    @Override
    public Class<?> mixInClass() {
        return ExerciseFormatDTOMixin.class;
    }

    @Override
    public String typeName() {
        return ExerciseFormatType.SINGLE_INTERVAL.name();
    }
}

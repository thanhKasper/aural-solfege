package vn.ktt.ear_training_system.infrastructure.jackson.mixinProvider;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ExerciseFormatType;
import vn.ktt.ear_training_system.domain.ExerciseActivity;
import vn.ktt.ear_training_system.domain.interval_training.SingleIntervalExerciseActivity;
import vn.ktt.ear_training_system.infrastructure.jackson.mixin.SingleIntervalExerciseFormatMixin;

@Component
public class SingleIntervalExerciseFormatMixInProvider implements ExerciseFormatMixInProvider {
    @Override
    public Class<? extends ExerciseActivity> targetClass() {
        return SingleIntervalExerciseActivity.class;
    }

    @Override
    public Class<?> mixInClass() {
        return SingleIntervalExerciseFormatMixin.class;
    }

    @Override
    public String typeName() {
        return ExerciseFormatType.SINGLE_INTERVAL.name();
    }
}

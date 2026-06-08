package vn.ktt.ear_training_system.infrastructure.jackson.mixinProvider;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.ExerciseFormat;
import vn.ktt.ear_training_system.domain.interval_training.SingleIntervalExerciseFormat;
import vn.ktt.ear_training_system.infrastructure.jackson.mixin.SingleIntervalExerciseFormatMixin;

@Component
public class SingleIntervalExerciseFormatMixInProvider implements ExerciseFormatMixInProvider {
    @Override
    public Class<? extends ExerciseFormat> targetClass() {
        return SingleIntervalExerciseFormat.class;
    }

    @Override
    public Class<?> mixInClass() {
        return SingleIntervalExerciseFormatMixin.class;
    }
}

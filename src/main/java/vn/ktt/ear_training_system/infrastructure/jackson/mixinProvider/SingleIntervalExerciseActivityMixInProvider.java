package vn.ktt.ear_training_system.infrastructure.jackson.mixinProvider;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ExerciseActivityType;
import vn.ktt.ear_training_system.domain.ExerciseActivity;
import vn.ktt.ear_training_system.domain.interval_training.SingleIntervalExerciseActivity;
import vn.ktt.ear_training_system.infrastructure.jackson.mixin.SingleIntervalExerciseActivityMixin;

@Component
public class SingleIntervalExerciseActivityMixInProvider implements ExerciseActivityMixInProvider {
    @Override
    public Class<? extends ExerciseActivity> targetClass() {
        return SingleIntervalExerciseActivity.class;
    }

    @Override
    public Class<?> mixInClass() {
        return SingleIntervalExerciseActivityMixin.class;
    }

    @Override
    public String typeName() {
        return ExerciseActivityType.SINGLE_INTERVAL.name();
    }
}

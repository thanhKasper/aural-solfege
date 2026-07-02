package vn.ktt.ear_training_system.infrastructure.jackson.mixin_provider.entity.exercise_activity;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ExerciseActivityType;
import vn.ktt.ear_training_system.domain.exercise.entity.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.entity.interval_training.SingleIntervalExerciseActivity;
import vn.ktt.ear_training_system.infrastructure.jackson.mixin.entity.exercise_activity.SingleIntervalExerciseActivityMixin;

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

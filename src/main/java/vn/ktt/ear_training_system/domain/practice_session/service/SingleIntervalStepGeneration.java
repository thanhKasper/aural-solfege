package vn.ktt.ear_training_system.domain.practice_session.service;

import vn.ktt.ear_training_system.domain.exercise.entity.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.entity.interval_training.SingleIntervalExerciseActivity;
import vn.ktt.ear_training_system.domain.practice_session.value_object.*;

import java.util.List;

public class SingleIntervalStepGeneration implements StepGeneration {

    @Override
    public Class<? extends ExerciseActivity> activityType() {
        return SingleIntervalExerciseActivity.class;
    }

    @Override
    public List<StepDefinition> generate(ExerciseActivity activity) {
        var a = (SingleIntervalExerciseActivity) activity;
        return List.of(
                new StepDefinition(a.getPosition(), StepType.LISTEN_INTERVAL,
                        new ListenIntervalContext(a.getInterval(), "UP", a.getSoundProperty())),
                new StepDefinition(a.getPosition(), StepType.LISTEN_INTERVAL,
                        new ListenIntervalContext(a.getInterval(), "DOWN", a.getSoundProperty()))
        );
    }
}

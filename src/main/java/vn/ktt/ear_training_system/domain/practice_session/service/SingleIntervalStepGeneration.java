package vn.ktt.ear_training_system.domain.practice_session.service;

import vn.ktt.ear_training_system.domain.exercise.value_object.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.value_object.exercise_activity.SingleIntervalExerciseActivity;
import vn.ktt.ear_training_system.domain.practice_session.value_object.*;

import java.util.List;
import java.util.stream.Stream;

public class SingleIntervalStepGeneration implements StepGeneration {

    @Override
    public List<StepDefinition> generate(ExerciseActivity activity) {
        var a = (SingleIntervalExerciseActivity) activity;
        return a.getIntervals().stream()
                .flatMap(interval -> Stream.of(
                        new StepDefinition(a.getPosition(), StepType.LISTEN_INTERVAL,
                                new ListenIntervalContext(interval, "UP", a.getSoundProperty())),
                        new StepDefinition(a.getPosition(), StepType.LISTEN_INTERVAL,
                                new ListenIntervalContext(interval, "DOWN", a.getSoundProperty()))
                ))
                .toList();
    }

    @Override
    public Class<? extends ExerciseActivity> getKey() {
        return SingleIntervalExerciseActivity.class;
    }

    @Override
    public StepGeneration getService() {
        return this;
    }
}

package vn.ktt.eartraining.domain.session.service;

import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.exercise.valueobject.activity.SingleIntervalExerciseActivity;
import vn.ktt.eartraining.domain.session.valueobject.*;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.ListenIntervalContext;

import java.util.List;
import java.util.stream.Stream;

public class SingleIntervalStepGeneration implements IStepGeneration {

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
    public IStepGeneration getService() {
        return this;
    }
}

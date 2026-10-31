package vn.ktt.eartraining.domain.session.service;

import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.exercise.valueobject.activity.SingleIntervalExerciseActivity;
import vn.ktt.eartraining.domain.session.valueobject.StepGroupBlueprint;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.ListenIntervalContext;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.StepContext;

import java.util.stream.Stream;

public class SingleIntervalStepGeneration implements IStepGeneration {

    @Override
    public StepGroupBlueprint generate(ExerciseActivity activity) {
        var a = (SingleIntervalExerciseActivity) activity;
        return new StepGroupBlueprint(a.getIntervals().stream()
                .<StepContext>flatMap(interval -> Stream.of(
                        new ListenIntervalContext(interval, "UP", a.getSoundProperty()),
                        new ListenIntervalContext(interval, "DOWN", a.getSoundProperty())
                ))
                .toList());
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

package vn.ktt.eartraining.domain.session.service;

import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.exercise.valueobject.activity.CoolDownRestActivity;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.CoolDownContext;
import vn.ktt.eartraining.domain.session.valueobject.StepDefinition;
import vn.ktt.eartraining.domain.session.valueobject.StepType;

import java.util.List;

public class CoolDownStepGeneration implements IStepGeneration {

    @Override
    public List<StepDefinition> generate(ExerciseActivity activity) {
        var a = (CoolDownRestActivity) activity;
        return List.of(new StepDefinition(a.getPosition(), StepType.COOL_DOWN,
                new CoolDownContext(a.getRestAmountInSecond())));
    }

    @Override
    public Class<? extends ExerciseActivity> getKey() {
        return CoolDownRestActivity.class;
    }

    @Override
    public IStepGeneration getService() {
        return this;
    }
}

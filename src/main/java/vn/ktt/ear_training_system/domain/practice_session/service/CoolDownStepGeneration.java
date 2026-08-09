package vn.ktt.ear_training_system.domain.practice_session.service;

import vn.ktt.ear_training_system.domain.exercise.entity.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.entity.resting_activity.CoolDownRestActivity;
import vn.ktt.ear_training_system.domain.practice_session.value_object.CoolDownContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepDefinition;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepType;

import java.util.List;

public class CoolDownStepGeneration implements StepGeneration {

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
    public StepGeneration getService() {
        return this;
    }
}

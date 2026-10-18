package vn.ktt.eartraining.domain.session.service;

import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.session.valueobject.StepDefinition;
import vn.ktt.shared.IServiceIndex;

import java.util.List;

public interface IStepGeneration extends IServiceIndex<ExerciseActivity, IStepGeneration> {
    List<StepDefinition> generate(ExerciseActivity activity);
}

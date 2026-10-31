package vn.ktt.eartraining.domain.session.service;

import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.session.valueobject.StepGroupBlueprint;
import vn.ktt.shared.IServiceIndex;

public interface IStepGeneration extends IServiceIndex<ExerciseActivity, IStepGeneration> {
    StepGroupBlueprint generate(ExerciseActivity activity);
}

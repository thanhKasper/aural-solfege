package vn.ktt.ear_training_system.domain.practice_session.service;

import vn.ktt.ear_training_system.domain.exercise.entity.ExerciseActivity;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepDefinition;
import vn.ktt.shared.IServiceIndex;

import java.util.List;

public interface StepGeneration extends IServiceIndex<ExerciseActivity, StepGeneration> {
    List<StepDefinition> generate(ExerciseActivity activity);
}

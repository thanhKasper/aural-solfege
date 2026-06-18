package vn.ktt.ear_training_system.domain.practice_session.service;

import vn.ktt.ear_training_system.domain.ExerciseActivity;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepDefinition;

import java.util.List;

public interface StepGeneration {
    Class<? extends ExerciseActivity> activityType();
    List<StepDefinition> generate(ExerciseActivity activity);
}

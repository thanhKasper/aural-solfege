package vn.ktt.ear_training_system.domain.exercise.service;

import vn.ktt.ear_training_system.domain.exercise.entity.Exercise;
import vn.ktt.ear_training_system.domain.exercise.entity.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.value_object.TrainingMethodology;

import java.util.List;

public class ExerciseBuilder {
    public Exercise buildExercise(
            String trainingMethod,
            String title,
            String description,
            boolean loop,
            int repetitions,
            int rest,
            List<ExerciseActivity> exerciseActivities
    ) {
        return Exercise.create(TrainingMethodology.valueOf(trainingMethod), title, description, loop, repetitions, rest, exerciseActivities);
    }
}

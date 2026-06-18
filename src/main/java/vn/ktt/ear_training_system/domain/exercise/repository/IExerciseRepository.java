package vn.ktt.ear_training_system.domain.exercise.repository;

import vn.ktt.ear_training_system.domain.exercise.entity.Exercise;

public interface IExerciseRepository {
    void saveExercise(Exercise exercise);
    Exercise getExerciseById(String id);
}

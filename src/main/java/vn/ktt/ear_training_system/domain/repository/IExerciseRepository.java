package vn.ktt.ear_training_system.domain.repository;

import vn.ktt.ear_training_system.domain.Exercise;

public interface IExerciseRepository {
    void saveExercise(Exercise exercise);
    Exercise getExerciseById(String id);
}

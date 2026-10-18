package vn.ktt.eartraining.domain.exercise.repository;

import vn.ktt.eartraining.domain.exercise.entity.Exercise;

public interface IExerciseRepository {
    void saveExercise(Exercise exercise);
    Exercise getExerciseById(String id);
}

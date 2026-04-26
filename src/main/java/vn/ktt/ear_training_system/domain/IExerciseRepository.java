package vn.ktt.ear_training_system.domain;

import java.util.List;
import java.util.Optional;

public interface IExerciseRepository {
    void saveExercise(Exercise exercise);
    void deleteExercise(Exercise exercise);
    List<Exercise> findExerciseByTitle(String title);
    Optional<Exercise> findExerciseById(String id);
    List<Exercise> findAll();
}

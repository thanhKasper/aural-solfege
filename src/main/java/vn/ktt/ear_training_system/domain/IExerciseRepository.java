package vn.ktt.ear_training_system.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IExerciseRepository {
    void saveExercise(Exercise exercise);
    void deleteExercise(Exercise exercise);
    List<Exercise> findExerciseByTitle(String title);
    Optional<Exercise> findExerciseById(UUID id);
    List<Exercise> findAll();
}

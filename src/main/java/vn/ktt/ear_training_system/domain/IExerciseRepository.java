package vn.ktt.ear_training_system.domain;

import java.util.List;

public interface IExerciseRepository {
    void save(Exercise exercise);
    void delete(Exercise exercise);
    List<Exercise> findByTitle(String title);
    Exercise findById(String id);
}

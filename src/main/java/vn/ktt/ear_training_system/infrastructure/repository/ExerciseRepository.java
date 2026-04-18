package vn.ktt.ear_training_system.infrastructure.repository;

import org.springframework.stereotype.Repository;
import vn.ktt.ear_training_system.domain.Exercise;
import vn.ktt.ear_training_system.domain.IExerciseRepository;

import java.util.List;

@Repository
public class ExerciseRepository implements IExerciseRepository {
    @Override
    public void save(Exercise exercise) {
        System.out.println("Storing new exercise...");
    }

    @Override
    public void delete(Exercise exercise) {

    }

    @Override
    public List<Exercise> findByTitle(String title) {
        return List.of();
    }

    @Override
    public Exercise findById(String id) {
        return null;
    }
}

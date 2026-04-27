package vn.ktt.ear_training_system.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.ktt.ear_training_system.domain.Exercise;
import vn.ktt.ear_training_system.domain.IExerciseRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, UUID>, IExerciseRepository {
    @Override
    default void saveExercise(Exercise exercise) {
        this.save(exercise);
    }

    @Override
    default void deleteExercise(Exercise exercise) {
        this.delete(exercise);
    }

    @Override
    default Optional<Exercise> findExerciseById(UUID exerciseId) {
        return this.findById(exerciseId);
    }
}

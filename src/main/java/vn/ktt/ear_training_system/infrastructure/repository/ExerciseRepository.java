package vn.ktt.ear_training_system.infrastructure.repository;

import org.springframework.stereotype.Repository;
import vn.ktt.ear_training_system.domain.Exercise;
import vn.ktt.ear_training_system.domain.IExerciseRepository;
import vn.ktt.ear_training_system.infrastructure.repository.mapper.ExerciseEntityMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ExerciseRepository implements IExerciseRepository {

    private final ExerciseJpaRepository jpaRepository;
    private final ExerciseEntityMapper mapper;

    public ExerciseRepository(ExerciseJpaRepository jpaRepository, ExerciseEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void saveExercise(Exercise exercise) {
        var entity = mapper.toEntity(exercise);
        jpaRepository.save(entity);
    }

    @Override
    public void deleteExercise(Exercise exercise) {
        var entity = mapper.toEntity(exercise);
        jpaRepository.delete(entity);
    }

    @Override
    public List<Exercise> findExerciseByTitle(String title) {
        return jpaRepository.findAll().stream()
                .filter(entity -> entity.getTitle().equals(title))
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Exercise> findExerciseById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Exercise> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}

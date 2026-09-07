package vn.ktt.eartraining.infrastructure.persistence.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import vn.ktt.eartraining.application.outbound.IExercisePaginationPort;
import vn.ktt.eartraining.application.services.Page;
import vn.ktt.eartraining.domain.exercise.entity.Exercise;
import vn.ktt.eartraining.domain.exercise.repository.IExerciseRepository;
import vn.ktt.eartraining.infrastructure.persistence.entity.ExerciseEntity;
import vn.ktt.eartraining.infrastructure.persistence.mapper.ExerciseEntityMapper;

import java.util.UUID;

@Repository
public class ExerciseRepository implements IExerciseRepository, IExercisePaginationPort {

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
    public Exercise getExerciseById(String id) {
        var exerciseEntity = jpaRepository.findById(UUID.fromString(id)).orElseThrow();
        return mapper.toDomain(exerciseEntity);
    }

    @Override
    public Page<Exercise> getPagedExercises(int page, int pageSize) {
        org.springframework.data.domain.Page<ExerciseEntity> paginatedExercises = jpaRepository.findAll(PageRequest.of(page, pageSize));

        var domains = paginatedExercises.stream().map(mapper::toDomain).toList();
        return new Page<>(
                page,
                pageSize,
                paginatedExercises.getTotalPages(),
                (int) paginatedExercises.getTotalElements(),
                paginatedExercises.hasNext(),
                paginatedExercises.hasPrevious(),
                domains
        );
    }
}

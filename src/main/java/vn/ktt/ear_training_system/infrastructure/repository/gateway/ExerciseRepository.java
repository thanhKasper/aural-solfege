package vn.ktt.ear_training_system.infrastructure.repository.gateway;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.outbound.IExercisePaginationPort;
import vn.ktt.ear_training_system.application.services.Page;
import vn.ktt.ear_training_system.domain.exercise.entity.Exercise;
import vn.ktt.ear_training_system.domain.exercise.repository.IExerciseRepository;
import vn.ktt.ear_training_system.infrastructure.repository.entities.ExerciseEntity;
import vn.ktt.ear_training_system.infrastructure.repository.mapper.ExerciseEntityMapper;

import java.util.List;
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
    public Page<ExerciseDTO> getPagedExercises(int page, int pageSize) {
        org.springframework.data.domain.Page<ExerciseEntity> paginatedExercises = jpaRepository.findAll(PageRequest.of(page, pageSize));

        int totalElements = paginatedExercises.getNumberOfElements();
        int totalPages = paginatedExercises.getTotalPages();

        List<ExerciseDTO> result = paginatedExercises.stream().map(mapper::toDto).toList();
        return new Page<>(
                page,
                pageSize,
                totalPages,
                totalElements,
                paginatedExercises.hasNext(),
                paginatedExercises.hasPrevious(),
                result
        );
    }
}

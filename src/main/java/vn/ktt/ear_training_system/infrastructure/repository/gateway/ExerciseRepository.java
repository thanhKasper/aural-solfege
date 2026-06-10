package vn.ktt.ear_training_system.infrastructure.repository.gateway;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.services.IExercisePaginationService;
import vn.ktt.ear_training_system.application.services.Page;
import vn.ktt.ear_training_system.domain.Exercise;
import vn.ktt.ear_training_system.domain.repository.IExerciseRepository;
import vn.ktt.ear_training_system.infrastructure.repository.entities.ExerciseEntity;
import vn.ktt.ear_training_system.infrastructure.repository.mapper.ExerciseEntityMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ExerciseRepository implements IExerciseRepository, IExercisePaginationService {

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
    public Page<ExerciseDTO> getPagedExercises(int page, int pageSize) {
        org.springframework.data.domain.Page<ExerciseEntity> paginatedExercises = jpaRepository.findAll(PageRequest.of(page, pageSize));

        int totalElements = (int) paginatedExercises.getNumberOfElements();
        int totalPages = (int) paginatedExercises.getTotalPages();

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

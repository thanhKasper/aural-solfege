package vn.ktt.eartraining.infrastructure.persistence.gateway;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import vn.ktt.eartraining.application.access.NotFoundException;
import vn.ktt.eartraining.application.outbound.ICurrentUserPort;
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
    private final ICurrentUserPort currentUser;

    public ExerciseRepository(ExerciseJpaRepository jpaRepository, ExerciseEntityMapper mapper, ICurrentUserPort currentUser) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    @Override
    public void saveExercise(Exercise exercise) {
        var entity = mapper.toEntity(exercise);
        // The domain has no owner: stamp it on insert, keep the stored one on update.
        entity.setOwnerId(exercise.getExerciseId() == null
                ? currentUser.userId()
                : findOwned(exercise.getExerciseId().toString()).getOwnerId());
        jpaRepository.save(entity);
    }

    @Override
    public Exercise getExerciseById(String id) {
        return mapper.toDomain(findOwned(id));
    }

    private ExerciseEntity findOwned(String id) {
        return jpaRepository.findByExerciseIdAndOwnerId(UUID.fromString(id), currentUser.userId())
                .orElseThrow(() -> new NotFoundException("Exercise not found: " + id));
    }

    @Override
    public Page<Exercise> getPagedExercises(int page, int pageSize) {
        org.springframework.data.domain.Page<ExerciseEntity> paginatedExercises =
                jpaRepository.findByOwnerId(currentUser.userId(), PageRequest.of(page, pageSize));

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

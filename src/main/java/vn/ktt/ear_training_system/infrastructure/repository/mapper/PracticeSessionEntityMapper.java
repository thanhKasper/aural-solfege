package vn.ktt.ear_training_system.infrastructure.repository.mapper;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeSession;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeStep;
import vn.ktt.ear_training_system.infrastructure.repository.entities.PracticeSessionEntity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.PracticeStepEntity;
import vn.ktt.ear_training_system.infrastructure.repository.gateway.ExerciseJpaRepository;
import vn.ktt.ear_training_system.infrastructure.repository.mapper.step_context.StepContextEntityToDomainMapperFactory;

import java.util.ArrayList;

@Component
public class PracticeSessionEntityMapper {

    private final ExerciseJpaRepository exerciseJpaRepository;
    private final StepContextEntityToDomainMapperFactory stepContextMapperFactory;

    public PracticeSessionEntityMapper(ExerciseJpaRepository exerciseJpaRepository,
                                       StepContextEntityToDomainMapperFactory stepContextMapperFactory) {
        this.exerciseJpaRepository = exerciseJpaRepository;
        this.stepContextMapperFactory = stepContextMapperFactory;
    }

    public PracticeSessionEntity toEntity(PracticeSession domain) {
        var entity = new PracticeSessionEntity();
        entity.setSessionId(domain.getSessionId());
        entity.setExercise(exerciseJpaRepository.getReferenceById(domain.getExerciseId()));
        entity.setStatus(domain.getStatus());
        entity.setCurrentStepIndex(domain.getCurrentStepIndex());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setStartedAt(domain.getStartedAt());
        entity.setCompletedAt(domain.getCompletedAt());

        var stepEntities = new ArrayList<PracticeStepEntity>();
        for (int i = 0; i < domain.getSteps().size(); i++) {
            var step = domain.getSteps().get(i);
            var stepEntity = new PracticeStepEntity();
            stepEntity.setSession(entity);
            stepEntity.setStepNumber(i);
            stepEntity.setActivityPosition(step.getActivityPosition());
            stepEntity.setStepType(step.getStepType());
            stepEntity.setStatus(step.getStatus());
            stepEntity.setContext(stepContextMapperFactory.toStepContextEntity(step.getContext()));
            stepEntities.add(stepEntity);
        }

        entity.setSteps(stepEntities);
        return entity;
    }

    public PracticeSession toDomain(PracticeSessionEntity entity) {
        var steps = entity.getSteps().stream()
                .map(stepEntity -> new PracticeStep(
                        stepEntity.getActivityPosition(),
                        stepEntity.getStepType(),
                        stepEntity.getStatus(),
                        stepContextMapperFactory.toStepContext(stepEntity.getContext())))
                .toList();

        return PracticeSession.reconstruct(
                entity.getSessionId(),
                entity.getExercise().getExerciseId(),
                entity.getStatus(),
                entity.getCurrentStepIndex(),
                steps,
                entity.getCreatedAt(),
                entity.getStartedAt(),
                entity.getCompletedAt()
        );
    }
}

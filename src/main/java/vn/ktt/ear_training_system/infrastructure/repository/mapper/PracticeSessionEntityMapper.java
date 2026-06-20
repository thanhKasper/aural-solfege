package vn.ktt.ear_training_system.infrastructure.repository.mapper;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeSession;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeStep;
import vn.ktt.ear_training_system.infrastructure.repository.entities.PracticeSessionEntity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.PracticeStepEntity;

@Component
public class PracticeSessionEntityMapper {

    public PracticeSessionEntity toEntity(PracticeSession domain) {
        var entity = new PracticeSessionEntity();
        entity.setExerciseId(domain.getExerciseId());
        entity.setStatus(domain.getStatus());
        entity.setCurrentStepIndex(domain.getCurrentStepIndex());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setStartedAt(domain.getStartedAt());
        entity.setCompletedAt(domain.getCompletedAt());
        entity.setResult(domain.getResult());

        var stepEntities = domain.getSteps().stream()
                .map(step -> {
                    var stepEntity = new PracticeStepEntity();
                    stepEntity.setSession(entity);
                    stepEntity.setStepNumber(step.getStepNumber());
                    stepEntity.setActivityPosition(step.getActivityPosition());
                    stepEntity.setStepType(step.getStepType());
                    stepEntity.setStatus(step.getStatus());
                    stepEntity.setContext(step.getContext());
                    return stepEntity;
                }).toList();

        entity.setSteps(stepEntities);
        return entity;
    }

    public PracticeSession toDomain(PracticeSessionEntity entity) {
        var steps = entity.getSteps().stream()
                .map(stepEntity -> new PracticeStep(
                        stepEntity.getStepNumber(),
                        stepEntity.getActivityPosition(),
                        stepEntity.getStepType(),
                        stepEntity.getStatus(),
                        stepEntity.getContext()))
                .toList();

        return PracticeSession.reconstruct(
                entity.getSessionId(),
                entity.getExerciseId(),
                entity.getStatus(),
                entity.getCurrentStepIndex(),
                steps,
                entity.getCreatedAt(),
                entity.getStartedAt(),
                entity.getCompletedAt(),
                entity.getResult()
        );
    }
}

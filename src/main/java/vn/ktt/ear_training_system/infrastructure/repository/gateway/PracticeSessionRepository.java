package vn.ktt.ear_training_system.infrastructure.repository.gateway;

import org.springframework.stereotype.Repository;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeSession;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeStep;
import vn.ktt.ear_training_system.domain.practice_session.repository.IPracticeSessionRepository;
import vn.ktt.ear_training_system.domain.practice_session.value_object.SessionStatus;
import vn.ktt.ear_training_system.infrastructure.repository.entities.PracticeSessionEntity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.PracticeStepEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public class PracticeSessionRepository implements IPracticeSessionRepository {

    private final PracticeSessionJpaRepository jpaRepository;

    public PracticeSessionRepository(PracticeSessionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void saveSession(PracticeSession session) {
        jpaRepository.save(toEntity(session));
    }

    @Override
    public Optional<PracticeSession> getSessionById(UUID sessionId) {
        return jpaRepository.findWithStepsById(sessionId).map(this::toDomain);
    }

    @Override
    public Optional<PracticeSession> findByExercise(UUID exerciseId) {
        return jpaRepository.findTopByExerciseIdOrderByCreatedAtDesc(exerciseId)
                .map(this::toDomain);
    }

    @Override
    public boolean existsActiveSessionForExercise(UUID exerciseId) {
        return jpaRepository.existsByExerciseIdAndStatus(exerciseId, SessionStatus.IN_PROGRESS);
    }

    private PracticeSessionEntity toEntity(PracticeSession domain) {
        var entity = new PracticeSessionEntity();
        entity.setSessionId(domain.getSessionId());
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

    private PracticeSession toDomain(PracticeSessionEntity entity) {
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

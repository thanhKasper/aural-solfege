package vn.ktt.eartraining.infrastructure.persistence.gateway;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import vn.ktt.eartraining.domain.session.valueobject.SessionStatus;
import vn.ktt.eartraining.infrastructure.persistence.entity.PracticeSessionEntity;

import java.util.Optional;
import java.util.UUID;

/** Every lookup is scoped by learner: a session owned by someone else is never returned. */
public interface PracticeSessionJpaRepository extends JpaRepository<PracticeSessionEntity, UUID> {

    @EntityGraph(attributePaths = {"steps", "exercise"})
    Optional<PracticeSessionEntity> findBySessionIdAndLearnerId(UUID id, String learnerId);

    @EntityGraph(attributePaths = {"steps", "exercise"})
    Optional<PracticeSessionEntity> findTopByLearnerIdAndExerciseExerciseIdOrderByCreatedAtDesc(String learnerId, UUID exerciseId);

    boolean existsByLearnerIdAndExerciseExerciseIdAndStatus(String learnerId, UUID exerciseId, SessionStatus status);

    /** Loads then removes the entity, so the steps are removed through the cascade. */
    @Transactional
    long deleteBySessionIdAndLearnerId(UUID sessionId, String learnerId);
}

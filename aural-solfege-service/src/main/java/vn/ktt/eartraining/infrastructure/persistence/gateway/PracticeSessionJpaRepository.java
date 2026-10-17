package vn.ktt.eartraining.infrastructure.persistence.gateway;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.ktt.eartraining.domain.session.valueobject.SessionStatus;
import vn.ktt.eartraining.infrastructure.persistence.entity.PracticeSessionEntity;

import java.util.Optional;
import java.util.UUID;

public interface PracticeSessionJpaRepository extends JpaRepository<PracticeSessionEntity, UUID> {

    @EntityGraph(attributePaths = {"steps", "exercise"})
    Optional<PracticeSessionEntity> findBySessionId(UUID id);

    @EntityGraph(attributePaths = {"steps", "exercise"})
    Optional<PracticeSessionEntity> findTopByExerciseExerciseIdOrderByCreatedAtDesc(UUID exerciseId);

    boolean existsByExerciseExerciseIdAndStatus(UUID exerciseId, SessionStatus status);
}

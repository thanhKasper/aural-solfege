package vn.ktt.ear_training_system.infrastructure.repository.gateway;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.ktt.ear_training_system.domain.practice_session.value_object.SessionStatus;
import vn.ktt.ear_training_system.infrastructure.repository.entities.PracticeSessionEntity;

import java.util.Optional;
import java.util.UUID;

public interface PracticeSessionJpaRepository extends JpaRepository<PracticeSessionEntity, UUID> {

    @EntityGraph(attributePaths = "steps")
    Optional<PracticeSessionEntity> findBySessionId(UUID id);

    @EntityGraph(attributePaths = "steps")
    Optional<PracticeSessionEntity> findTopByExerciseIdOrderByCreatedAtDesc(UUID exerciseId);

    boolean existsByExerciseIdAndStatus(UUID exerciseId, SessionStatus status);
}

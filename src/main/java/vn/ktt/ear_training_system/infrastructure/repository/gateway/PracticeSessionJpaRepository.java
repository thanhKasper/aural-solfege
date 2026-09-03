package vn.ktt.ear_training_system.infrastructure.repository.gateway;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.ktt.ear_training_system.domain.practice_session.value_object.SessionStatus;
import vn.ktt.ear_training_system.infrastructure.repository.entities.PracticeSessionEntity;

import java.util.Optional;
import java.util.UUID;

public interface PracticeSessionJpaRepository extends JpaRepository<PracticeSessionEntity, UUID> {

    @EntityGraph(attributePaths = {"steps", "exercise"})
    Optional<PracticeSessionEntity> findBySessionId(UUID id);

    @EntityGraph(attributePaths = {"steps", "exercise"})
    Optional<PracticeSessionEntity> findTopByExerciseExerciseIdOrderByCreatedAtDesc(UUID exerciseId);

    boolean existsByExerciseExerciseIdAndStatus(UUID exerciseId, SessionStatus status);
}

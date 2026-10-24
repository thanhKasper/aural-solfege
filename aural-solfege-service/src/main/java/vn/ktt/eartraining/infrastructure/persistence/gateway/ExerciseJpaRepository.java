package vn.ktt.eartraining.infrastructure.persistence.gateway;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.ktt.eartraining.infrastructure.persistence.entity.ExerciseEntity;

import java.util.Optional;
import java.util.UUID;

public interface ExerciseJpaRepository extends JpaRepository<ExerciseEntity, UUID> {

    Optional<ExerciseEntity> findByExerciseIdAndOwnerId(UUID exerciseId, String ownerId);

    Page<ExerciseEntity> findByOwnerId(String ownerId, Pageable pageable);
}

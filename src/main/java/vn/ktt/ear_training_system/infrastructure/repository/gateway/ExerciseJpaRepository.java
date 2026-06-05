package vn.ktt.ear_training_system.infrastructure.repository.gateway;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.ktt.ear_training_system.infrastructure.repository.entities.ExerciseEntity;

import java.util.UUID;

public interface ExerciseJpaRepository extends JpaRepository<ExerciseEntity, UUID> {
}

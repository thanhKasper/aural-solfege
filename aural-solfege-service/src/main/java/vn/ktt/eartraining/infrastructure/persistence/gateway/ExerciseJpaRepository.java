package vn.ktt.eartraining.infrastructure.persistence.gateway;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.ktt.eartraining.infrastructure.persistence.entity.ExerciseEntity;

import java.util.UUID;

public interface ExerciseJpaRepository extends JpaRepository<ExerciseEntity, UUID> {
}

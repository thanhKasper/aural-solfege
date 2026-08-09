package vn.ktt.musical_components_core.musical_infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.ktt.musical_components_core.musical_infrastructure.repository.entities.MusicalConfigurationEntity;
import java.util.UUID;

public interface MusicalConfigurationRepository extends JpaRepository<MusicalConfigurationEntity, UUID> {
}

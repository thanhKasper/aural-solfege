package vn.ktt.music.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.ktt.music.infrastructure.persistence.entity.MusicalConfigurationEntity;
import java.util.UUID;

public interface MusicalConfigurationRepository extends JpaRepository<MusicalConfigurationEntity, UUID> {
}

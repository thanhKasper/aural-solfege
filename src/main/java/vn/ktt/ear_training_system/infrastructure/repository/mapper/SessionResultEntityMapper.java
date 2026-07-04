package vn.ktt.ear_training_system.infrastructure.repository.mapper;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.practice_session.value_object.SessionResult;
import vn.ktt.ear_training_system.infrastructure.repository.entities.SessionResultEntity;

import java.time.Duration;
import java.time.Instant;

@Component
public class SessionResultEntityMapper {

    public SessionResultEntity toEntity(SessionResult domain, Instant startedAt, Instant completedAt) {
        if (domain == null) return null;
        var entity = new SessionResultEntity();
        entity.setTotalSteps(domain.totalSteps());
        entity.setCompletedSteps(domain.completedSteps());
        entity.setStartedAt(startedAt);
        entity.setCompletedAt(completedAt);
        return entity;
    }

    public SessionResult toDomain(SessionResultEntity entity) {
        if (entity == null) return null;
        return new SessionResult(
                entity.getTotalSteps(),
                entity.getCompletedSteps(),
                Duration.between(entity.getStartedAt(), entity.getCompletedAt())
        );
    }
}

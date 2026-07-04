package vn.ktt.ear_training_system.infrastructure.repository.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class SessionResultEntity {
    private int totalSteps;
    private int completedSteps;
    private Instant startedAt;
    private Instant completedAt;

    public SessionResultEntity(int totalSteps, int completedSteps, Instant startedAt, Instant completedAt) {
        this.totalSteps = totalSteps;
        this.completedSteps = completedSteps;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }
}

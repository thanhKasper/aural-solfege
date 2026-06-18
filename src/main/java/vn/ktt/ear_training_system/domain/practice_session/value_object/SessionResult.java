package vn.ktt.ear_training_system.domain.practice_session.value_object;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Duration;

@Getter
@EqualsAndHashCode
public class SessionResult {
    private final int totalSteps;
    private final int completedSteps;
    private final Duration duration;

    public SessionResult(int totalSteps, int completedSteps, Duration duration) {
        this.totalSteps = totalSteps;
        this.completedSteps = completedSteps;
        this.duration = duration;
    }
}

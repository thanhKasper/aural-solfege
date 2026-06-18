package vn.ktt.ear_training_system.domain.practice_session.value_object;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Duration;
import java.util.Objects;

@Getter
@EqualsAndHashCode
public class SessionResult {
    private final int totalSteps;
    private final int completedSteps;
    private final Duration duration;

    public SessionResult(int totalSteps, int completedSteps, Duration duration) {
        if (totalSteps < 1) {
            throw new IllegalArgumentException("totalSteps must be at least 1 (was " + totalSteps + ")");
        }
        if (completedSteps < 0 || completedSteps > totalSteps) {
            throw new IllegalArgumentException(
                    "completedSteps must be between 0 and " + totalSteps + " (was " + completedSteps + ")");
        }
        this.totalSteps = totalSteps;
        this.completedSteps = completedSteps;
        this.duration = Objects.requireNonNull(duration, "duration must not be null");
    }
}

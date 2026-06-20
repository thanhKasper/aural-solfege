package vn.ktt.ear_training_system.domain.practice_session.value_object;

import java.time.Duration;
import java.util.Objects;

public record SessionResult(int totalSteps, int completedSteps, Duration duration) {
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

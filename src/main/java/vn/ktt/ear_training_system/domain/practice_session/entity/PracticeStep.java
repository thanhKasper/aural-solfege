package vn.ktt.ear_training_system.domain.practice_session.entity;

import lombok.Getter;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepStatus;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepType;

@Getter
public class PracticeStep {
    private final int activityPosition;
    private final StepType stepType;
    private StepStatus status;
    private final StepContext context;

    PracticeStep(int activityPosition, StepType stepType, StepContext context) {
        this(activityPosition, stepType, StepStatus.PENDING, context);
    }

    public PracticeStep(int activityPosition, StepType stepType, StepStatus status, StepContext context) {
        this.activityPosition = activityPosition;
        this.stepType = stepType;
        this.status = status;
        this.context = context;
    }

    public void activate() {
        this.status = StepStatus.ACTIVE;
    }

    public void markCompleted() {
        if (this.status != StepStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only ACTIVE steps can be completed (current: " + this.status + ")");
        }
        this.status = StepStatus.COMPLETED;
    }

    public void skip() {
        if (this.status != StepStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only ACTIVE steps can be skipped (current: " + this.status + ")");
        }
        this.status = StepStatus.SKIPPED;
    }
}

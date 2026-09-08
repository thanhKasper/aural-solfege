package vn.ktt.eartraining.domain.session.entity;

import lombok.Getter;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.StepContext;
import vn.ktt.eartraining.domain.session.valueobject.StepStatus;
import vn.ktt.eartraining.domain.session.valueobject.StepType;

// @TODO: Need more refinement, the state machine of this PracticeStep is questionable
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

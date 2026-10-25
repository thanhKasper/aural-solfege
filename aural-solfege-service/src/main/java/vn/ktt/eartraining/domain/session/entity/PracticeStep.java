package vn.ktt.eartraining.domain.session.entity;

import lombok.Getter;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.StepContext;
import vn.ktt.eartraining.domain.session.valueobject.StepStatus;

import java.util.Objects;

@Getter
public class PracticeStep {
    private final int stepGroup;
    private final int repetitionIndex;
    private StepStatus status;
    private final StepContext context;

    private PracticeStep(int stepGroup, int repetitionIndex, StepStatus status, StepContext context) {
        if (stepGroup < 0 || repetitionIndex < 0) {
            throw new IllegalArgumentException("Step group and repetition index cannot be negative");
        }
        this.stepGroup = stepGroup;
        this.repetitionIndex = repetitionIndex;
        this.status = Objects.requireNonNull(status);
        this.context = Objects.requireNonNull(context);
    }

    /** A step that has not been played yet. */
    public static PracticeStep planned(int stepGroup, int repetitionIndex, StepContext context) {
        return new PracticeStep(stepGroup, repetitionIndex, StepStatus.PENDING, context);
    }

    public static PracticeStep reconstruct(int stepGroup, int repetitionIndex, StepStatus status, StepContext context) {
        return new PracticeStep(stepGroup, repetitionIndex, status, context);
    }

    /** A rest between two repetitions, as opposed to a step of the exercise itself. */
    public boolean isRest() {
        return context.isRest();
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

package vn.ktt.eartraining.domain.session.valueobject.stepcontext;

import vn.ktt.eartraining.domain.session.valueobject.StepType;

public record CoolDownContext(int restingTimeInSecond) implements StepContext {

    public CoolDownContext {
        if (restingTimeInSecond <= 0) {
            throw new IllegalArgumentException("Resting time must be positive (was " + restingTimeInSecond + ")");
        }
    }

    @Override
    public StepType getStepType() {
        return StepType.COOL_DOWN;
    }
}

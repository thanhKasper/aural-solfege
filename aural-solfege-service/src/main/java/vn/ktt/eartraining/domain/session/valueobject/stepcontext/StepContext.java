package vn.ktt.eartraining.domain.session.valueobject.stepcontext;

import vn.ktt.eartraining.domain.session.valueobject.StepType;

public sealed interface StepContext permits ListenIntervalContext, CoolDownContext, IntervalSoundComparisonContext {
    public StepType getStepType();

    /** A rest between two repetitions. Reserved for the session layout: activities never generate one. */
    default boolean isRest() {
        return getStepType() == StepType.COOL_DOWN;
    }
}

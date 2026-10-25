package vn.ktt.eartraining.domain.session.valueobject.stepcontext;

import vn.ktt.eartraining.domain.exercise.valueobject.IntervalTexture;
import vn.ktt.eartraining.domain.exercise.valueobject.MusicalInterval;
import vn.ktt.eartraining.domain.session.valueobject.StepType;

public record ListenIntervalContext(MusicalInterval interval, String direction, IntervalTexture texture)
        implements StepContext {

    @Override
    public StepType getStepType() {
        return StepType.LISTEN_INTERVAL;
    }
}

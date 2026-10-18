package vn.ktt.eartraining.domain.session.valueobject.stepcontext;

import vn.ktt.eartraining.domain.exercise.valueobject.IntervalTexture;
import vn.ktt.eartraining.domain.exercise.valueobject.MusicalInterval;

public record ListenIntervalContext(MusicalInterval interval, String direction, IntervalTexture texture)
        implements StepContext {
}

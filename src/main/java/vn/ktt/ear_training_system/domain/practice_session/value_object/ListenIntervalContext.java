package vn.ktt.ear_training_system.domain.practice_session.value_object;

import vn.ktt.ear_training_system.domain.interval_training.IntervalTexture;
import vn.ktt.ear_training_system.domain.interval_training.MusicalInterval;

public record ListenIntervalContext(MusicalInterval interval, String direction, IntervalTexture texture)
        implements StepContext {
}

package vn.ktt.ear_training_system.domain.practice_session.value_object.step_context;

import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;

public record ListenIntervalContext(MusicalInterval interval, String direction, IntervalTexture texture)
        implements StepContext {
}

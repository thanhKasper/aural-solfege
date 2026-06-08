package vn.ktt.ear_training_system.domain.interval_training;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import vn.ktt.ear_training_system.domain.ExerciseFormat;

import java.util.Objects;

@Getter
@EqualsAndHashCode(callSuper = false)
public final class SingleIntervalExerciseFormat extends ExerciseFormat {
    private final IntervalTexture soundProperty;
    private final MusicalInterval interval;
    private final int position;

    public SingleIntervalExerciseFormat(IntervalTexture soundProperty, MusicalInterval interval, int position) {
        this.interval = Objects.requireNonNull(interval, "Interval must not be null");
        this.soundProperty = Objects.requireNonNull(soundProperty, "Sound property must not be null");
        if (position < 0) {
            throw new IllegalArgumentException("Position must be non-negative");
        }
        this.position = position;
    }
}

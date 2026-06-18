package vn.ktt.ear_training_system.domain.exercise.entity.interval_training;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import vn.ktt.ear_training_system.domain.exercise.entity.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;

import java.util.Objects;

@Getter
@EqualsAndHashCode(callSuper = false)
public final class SingleIntervalExerciseActivity extends ExerciseActivity {
    private final IntervalTexture soundProperty;
    private final MusicalInterval interval;

    public SingleIntervalExerciseActivity(IntervalTexture soundProperty, MusicalInterval interval, int position) {
        this.interval = Objects.requireNonNull(interval, "Interval must not be null");
        this.soundProperty = Objects.requireNonNull(soundProperty, "Sound property must not be null");
        super(position);
    }
}

package vn.ktt.ear_training_system.domain.exercise.entity.interval_training;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import vn.ktt.ear_training_system.domain.exercise.entity.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;

import java.util.List;
import java.util.Objects;

@Getter
@EqualsAndHashCode(callSuper = true)
public final class SingleIntervalExerciseActivity extends ExerciseActivity {
    private final IntervalTexture soundProperty;
    private final MusicalInterval interval;

    public SingleIntervalExerciseActivity(IntervalTexture soundProperty, MusicalInterval interval, int position) {
        super(position);
        this.interval = Objects.requireNonNull(interval, "Interval must not be null");
        this.soundProperty = Objects.requireNonNull(soundProperty, "Sound property must not be null");
    }

    @Override
    public List<MusicalInterval> getIntervals() {
        return List.of(interval);
    }
}

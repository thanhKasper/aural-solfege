package vn.ktt.eartraining.domain.exercise.valueobject.activity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.exercise.valueobject.IntervalTexture;
import vn.ktt.eartraining.domain.exercise.valueobject.MusicalInterval;

import java.util.List;
import java.util.Objects;

@Getter
@EqualsAndHashCode(callSuper = true)
public final class SingleIntervalExerciseActivity extends ExerciseActivity {
    private final IntervalTexture soundProperty;
    private final List<MusicalInterval> intervals;

    public SingleIntervalExerciseActivity(IntervalTexture soundProperty, List<MusicalInterval> intervals, int position) {
        super(position);
        var validated = Objects.requireNonNull(intervals, "Intervals must not be null");
        if (validated.size() != 1) {
            throw new IllegalArgumentException("SingleIntervalExerciseActivity must have exactly one interval, but got " + validated.size());
        }
        this.intervals = List.copyOf(validated);
        this.soundProperty = Objects.requireNonNull(soundProperty, "Sound property must not be null");
    }

    @Override
    public List<MusicalInterval> getIntervals() {
        return intervals;
    }
}

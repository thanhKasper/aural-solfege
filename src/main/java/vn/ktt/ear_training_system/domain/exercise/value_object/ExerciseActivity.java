package vn.ktt.ear_training_system.domain.exercise.value_object;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@EqualsAndHashCode
public abstract class ExerciseActivity {
    private final int position;

    protected ExerciseActivity(int position) {
        if (position < 0) {
            throw new IllegalArgumentException("Position cannot be less than 0");
        }
        this.position = position;
    }

    public abstract List<MusicalInterval> getIntervals();
}

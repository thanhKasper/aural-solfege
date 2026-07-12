package vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SingleIntervalExerciseActivityEntity extends ExerciseActivityEntity {
    private IntervalTexture soundProperty;
    private List<MusicalInterval> intervals;

    public SingleIntervalExerciseActivityEntity(IntervalTexture soundProperty, List<MusicalInterval> intervals, int position) {
        super(position);
        this.soundProperty = soundProperty;
        this.intervals = intervals;
    }
}

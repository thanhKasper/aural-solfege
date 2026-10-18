package vn.ktt.eartraining.infrastructure.persistence.entity.activity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.ktt.eartraining.domain.exercise.valueobject.IntervalTexture;
import vn.ktt.eartraining.domain.exercise.valueobject.MusicalInterval;

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

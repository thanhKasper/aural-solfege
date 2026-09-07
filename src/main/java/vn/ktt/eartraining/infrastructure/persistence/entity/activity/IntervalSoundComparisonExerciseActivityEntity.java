package vn.ktt.eartraining.infrastructure.persistence.entity.activity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.ktt.eartraining.domain.exercise.valueobject.IntervalTexture;
import vn.ktt.eartraining.domain.exercise.valueobject.MusicalInterval;

@Getter
@Setter
@NoArgsConstructor
public class IntervalSoundComparisonExerciseActivityEntity extends ExerciseActivityEntity {
    private IntervalTexture texture;
    private MusicalInterval firstInterval;
    private MusicalInterval secondInterval;

    public IntervalSoundComparisonExerciseActivityEntity(IntervalTexture texture, MusicalInterval firstInterval, MusicalInterval secondInterval, int position) {
        super(position);
        this.texture = texture;
        this.firstInterval = firstInterval;
        this.secondInterval = secondInterval;
    }
}

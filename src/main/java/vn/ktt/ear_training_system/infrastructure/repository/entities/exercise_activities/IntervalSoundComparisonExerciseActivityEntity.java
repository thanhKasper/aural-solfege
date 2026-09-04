package vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;

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

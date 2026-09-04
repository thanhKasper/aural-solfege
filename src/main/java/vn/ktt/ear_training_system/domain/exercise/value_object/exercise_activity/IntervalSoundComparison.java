package vn.ktt.ear_training_system.domain.exercise.value_object.exercise_activity;

import lombok.Getter;
import vn.ktt.ear_training_system.domain.exercise.value_object.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;

import java.util.List;

@Getter
public class IntervalSoundComparison extends ExerciseActivity {
    private final IntervalTexture texture;
    private final MusicalInterval firstInterval;
    private final MusicalInterval secondInterval;

    protected IntervalSoundComparison(int position, IntervalTexture texture, MusicalInterval firstInterval, MusicalInterval secondInterval) {
        super(position);
        this.texture = texture;
        this.firstInterval = firstInterval;
        this.secondInterval = secondInterval;
    }


    public static IntervalSoundComparison construct(int position, IntervalTexture texture, MusicalInterval firstInterval, MusicalInterval secondInterval) {
        return new IntervalSoundComparison(position, texture, firstInterval, secondInterval);
    }

    @Override
    public List<MusicalInterval> getIntervals() {
        return List.of(firstInterval, secondInterval);
    }
}

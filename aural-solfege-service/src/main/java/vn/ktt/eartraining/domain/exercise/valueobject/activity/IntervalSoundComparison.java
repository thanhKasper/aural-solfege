package vn.ktt.eartraining.domain.exercise.valueobject.activity;

import lombok.Getter;
import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.exercise.valueobject.IntervalTexture;
import vn.ktt.eartraining.domain.exercise.valueobject.MusicalInterval;

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

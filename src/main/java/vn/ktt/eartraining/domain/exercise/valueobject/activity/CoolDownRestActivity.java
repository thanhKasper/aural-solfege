package vn.ktt.eartraining.domain.exercise.valueobject.activity;

import lombok.Getter;
import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.exercise.valueobject.MusicalInterval;

import java.util.List;

// @TODO: Keep this simple for now, in the future there may be a mechanism to see if the amount of rest is reasonable
@Getter
public class CoolDownRestActivity extends ExerciseActivity {

    private final int restAmountInSecond;

    public CoolDownRestActivity(int position, int restAmountInSecond) {
        super(position);
        this.restAmountInSecond = restAmountInSecond;
    }

    @Override
    public List<MusicalInterval> getIntervals() {
        return List.of();
    }
}

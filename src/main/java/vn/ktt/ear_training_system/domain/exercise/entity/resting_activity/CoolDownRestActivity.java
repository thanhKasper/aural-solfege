package vn.ktt.ear_training_system.domain.exercise.entity.resting_activity;

import lombok.Getter;
import vn.ktt.ear_training_system.domain.exercise.entity.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;

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

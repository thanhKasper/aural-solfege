package vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CoolDownRestActivityEntity extends ExerciseActivityEntity {

    private int restAmountInSecond;

    public CoolDownRestActivityEntity(int position, int restAmountInSecond) {
        super(position);
        this.restAmountInSecond = restAmountInSecond;
    }
}

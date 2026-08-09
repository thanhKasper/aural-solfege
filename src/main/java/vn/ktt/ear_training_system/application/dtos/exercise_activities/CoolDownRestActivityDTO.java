package vn.ktt.ear_training_system.application.dtos.exercise_activities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public final class CoolDownRestActivityDTO implements ExerciseActivityDTO {

    private Integer position;
    private Integer restTime;

    public CoolDownRestActivityDTO(Integer position, Integer restTime) {
        this.position = position;
        this.restTime = restTime;
    }

    @Override
    public Integer position() {
        return this.position;
    }

    @Override
    public ExerciseActivityType type() {
        return ExerciseActivityType.COOL_DOWN;
    }
}

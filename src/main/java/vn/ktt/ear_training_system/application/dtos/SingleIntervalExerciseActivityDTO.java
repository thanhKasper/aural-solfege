package vn.ktt.ear_training_system.application.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.ExerciseActivityDTO;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public final class SingleIntervalExerciseActivityDTO implements ExerciseActivityDTO {
    private List<String> intervals;
    private String texture;
    private Integer position;

    public SingleIntervalExerciseActivityDTO(List<String> intervals, String texture, Integer position) {
        this.intervals = intervals;
        this.texture = texture;
        this.position = position;
    }

    @Override
    public Integer position() {
        return this.position;
    }

    @Override
    public ExerciseActivityType type() {
        return ExerciseActivityType.SINGLE_INTERVAL;
    }

}

package vn.ktt.ear_training_system.application.dtos.exercise_activities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public final class IntervalsComparisonExerciseActivityDTO implements ExerciseActivityDTO {
    private List<String> intervals;
    private String texture;
    private Integer position;

    public IntervalsComparisonExerciseActivityDTO(List<String> intervals, String texture, Integer position) {
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
        return ExerciseActivityType.INTERVALS_COMPARISON;
    }
}

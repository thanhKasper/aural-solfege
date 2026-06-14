package vn.ktt.ear_training_system.application.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public final class SingleIntervalExerciseActivityDTO implements ExerciseActivityDTO {
    private String interval;
    private String texture;
    private Integer position;

    public SingleIntervalExerciseActivityDTO(String interval, String texture, Integer position) {
        this.interval = interval;
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

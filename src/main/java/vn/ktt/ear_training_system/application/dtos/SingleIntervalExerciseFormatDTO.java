package vn.ktt.ear_training_system.application.dtos;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class SingleIntervalExerciseFormatDTO implements ExerciseFormatDTO {
    private String interval;
    private String texture;
    private Integer position;

    public SingleIntervalExerciseFormatDTO() {}

    public SingleIntervalExerciseFormatDTO(String interval, String texture, Integer position) {
        this.interval = interval;
        this.texture = texture;
        this.position = position;
    }

    @Override
    public Integer position() {
        return this.position;
    }

    @Override
    public ExerciseFormatType type() {
        return ExerciseFormatType.SINGLE_INTERVAL;
    }

}

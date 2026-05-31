package vn.ktt.ear_training_system.application.dtos;

public final class SingleIntervalExerciseFormatDTO implements ExerciseFormatDTO {
    private String interval;
    private String texture;
    private final Integer position;

    public SingleIntervalExerciseFormatDTO(String interval, String texture, Integer position) {
        this.interval = interval;
        this.texture = texture;
        this.position = position;
    }

    @Override
    public String type() {
        return "SINGLE_INTERVAL";
    }

    public Integer position() {
        return this.position;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getTexture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }
}

package vn.ktt.ear_training_system.application.dtos;

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

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}

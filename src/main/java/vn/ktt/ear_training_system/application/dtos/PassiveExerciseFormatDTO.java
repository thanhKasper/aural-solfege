package vn.ktt.ear_training_system.application.dtos;

public final class PassiveExerciseFormatDTO implements ExerciseFormatDTO {
    private String interval;
    private String intervalProperty;
    @Override
    public String type() {
        return "passive";
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getIntervalProperty() {
        return intervalProperty;
    }

    public void setIntervalProperty(String intervalProperty) {
        this.intervalProperty = intervalProperty;
    }
}

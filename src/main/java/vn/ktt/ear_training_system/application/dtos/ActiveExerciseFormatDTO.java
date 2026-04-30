package vn.ktt.ear_training_system.application.dtos;

import java.util.List;

public final class ActiveExerciseFormatDTO implements ExerciseFormatDTO {
    private List<String> trainingIntervals;
    private Integer trainingAmounts;

    public ActiveExerciseFormatDTO(List<String> trainingIntervals, Integer trainingAmounts) {
        this.trainingIntervals = trainingIntervals;
        this.trainingAmounts = trainingAmounts;
    }
    public ActiveExerciseFormatDTO() {};

    @Override
    public String type() {
        return "active";
    }

    public List<String> getTrainingIntervals() {
        return this.trainingIntervals;
    }

    public Integer getTrainingAmounts() {
        return trainingAmounts;
    }
}

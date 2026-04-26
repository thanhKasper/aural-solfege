package vn.ktt.ear_training_system.application.dtos;

import java.util.List;

public class ExerciseDTO {
    private String title;
    private String description;
    private String trainingMethodology;
    private Integer reps;
    private List<ExerciseFormatDTO> exerciseFormats;

    public ExerciseDTO(String title, String description, String trainingMethodology, Integer reps, List<ExerciseFormatDTO> exerciseFormats) {
        this.title = title;
        this.description = description;
        this.trainingMethodology = trainingMethodology;
        this.reps = reps;
        this.exerciseFormats = exerciseFormats;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTrainingMethodology() {
        return trainingMethodology;
    }

    public void setTrainingMethodology(String trainingMethodology) {
        this.trainingMethodology = trainingMethodology;
    }

    public Integer getReps() {
        return reps;
    }

    public void setReps(Integer reps) {
        this.reps = reps;
    }

    public List<ExerciseFormatDTO> getExerciseFormats() {
        return exerciseFormats;
    }

    public void setExerciseFormats(List<ExerciseFormatDTO> exerciseFormats) {
        this.exerciseFormats = exerciseFormats;
    }

    @Override
    public String toString() {
        return String.format("ExerciseDTO:\n\tTitle: %s\n\tDescription:  %s\n\tTraining method: %s", title, description, trainingMethodology);
    }
}

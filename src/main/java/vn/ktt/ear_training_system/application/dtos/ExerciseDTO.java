package vn.ktt.ear_training_system.application.dtos;

import lombok.Data;

import java.util.List;

@Data
public class ExerciseDTO {
    private String exerciseId;
    private String title;
    private String description;
    private String trainingMethodology;
    private Integer reps;
    private Integer rest;
    private boolean loop;
    private List<ExerciseFormatDTO> exerciseFormats;

    public ExerciseDTO(String exerciseId, String title, String description, String trainingMethodology, Integer reps, List<ExerciseFormatDTO> exerciseFormats, Integer rest, boolean loop) {
        this.exerciseId = exerciseId;
        this.title = title;
        this.description = description;
        this.trainingMethodology = trainingMethodology;
        this.reps = reps;
        this.exerciseFormats = exerciseFormats;
        this.rest = rest;
        this.loop = loop;
    }
}

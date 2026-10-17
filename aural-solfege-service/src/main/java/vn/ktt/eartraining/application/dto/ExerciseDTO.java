package vn.ktt.eartraining.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import vn.ktt.eartraining.application.dto.activity.ExerciseActivityDTO;

import java.util.List;

@Data
@NoArgsConstructor
public class ExerciseDTO {
    private String exerciseId;
    private String title;
    private String description;
    private String trainingMethodology;
    private Integer reps;
    private Integer rest;
    private List<ExerciseActivityDTO> exerciseActivities;
    private List<String> intervals;

    public ExerciseDTO(String exerciseId, String title, String description, String trainingMethodology, Integer reps, List<ExerciseActivityDTO> exerciseActivities, Integer rest, List<String> intervals) {
        this.exerciseId = exerciseId;
        this.title = title;
        this.description = description;
        this.trainingMethodology = trainingMethodology;
        this.reps = reps;
        this.exerciseActivities = exerciseActivities;
        this.rest = rest;
        this.intervals = intervals;
    }
}

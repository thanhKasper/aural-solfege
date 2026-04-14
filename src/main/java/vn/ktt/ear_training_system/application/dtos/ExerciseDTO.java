package vn.ktt.ear_training_system.application.dtos;

import java.util.List;

public class ExerciseDTO {
    private String title;
    private String description;
    private String trainingMethodology;
    private Integer reps;
    private List<ExerciseFormatDTO> exerciseFormats;
}

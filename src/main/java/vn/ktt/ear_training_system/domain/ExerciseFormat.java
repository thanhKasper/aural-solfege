package vn.ktt.ear_training_system.domain;

import lombok.Getter;

@Getter
public abstract class ExerciseFormat {
    private final TrainingMethodology trainingMethodology;

    public ExerciseFormat(TrainingMethodology trainingMethodology) {
        this.trainingMethodology = trainingMethodology;
    }

}

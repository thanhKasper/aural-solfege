package vn.ktt.ear_training_system.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class ExerciseFormat {
    private TrainingMethodology trainingMethodology;

    public ExerciseFormat(TrainingMethodology trainingMethodology) {
        this.trainingMethodology = trainingMethodology;
    }

}

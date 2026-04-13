package vn.ktt.ear_training_system.domain;

import java.util.Map;

public abstract class ExerciseFormat {
    private final TrainingMethodology trainingMethodology;

    public ExerciseFormat(TrainingMethodology trainingMethodology) {
        this.trainingMethodology = trainingMethodology;
    }

    public TrainingMethodology getTrainingMethodology() {
        return trainingMethodology;
    }

    public abstract void buildExercise(Map<String, String> configurationVariables);
}

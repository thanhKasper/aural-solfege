package vn.ktt.ear_training_system.domain;

public class ExerciseFormat {
    private final TrainingMethodology trainingMethodology;

    public ExerciseFormat(TrainingMethodology trainingMethodology) {
        this.trainingMethodology = trainingMethodology;
    }

    public TrainingMethodology getTrainingMethodology() {
        return trainingMethodology;
    }
}

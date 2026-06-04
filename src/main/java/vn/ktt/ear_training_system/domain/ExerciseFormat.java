package vn.ktt.ear_training_system.domain;

public abstract class ExerciseFormat {
    private TrainingMethodology trainingMethodology;

    public ExerciseFormat(TrainingMethodology trainingMethodology) {
        this.trainingMethodology = trainingMethodology;
    }

    public TrainingMethodology getTrainingMethodology() {
        return trainingMethodology;
    }
}

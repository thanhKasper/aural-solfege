package vn.ktt.ear_training_system.domain;

public abstract class Exercise {
    private TrainingMethodology trainingMethodology;
    private ExerciseFormat exerciseFormat;

    public Exercise(TrainingMethodology trainingMethodology, ExerciseFormat exerciseFormat) {
        this.exerciseFormat = exerciseFormat;
        this.trainingMethodology = trainingMethodology;
    }
}

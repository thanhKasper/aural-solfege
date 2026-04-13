package vn.ktt.ear_training_system.domain;

public class Exercise {
    private final TrainingMethodology trainingMethodology;
    private final ExerciseFormat exerciseFormat;
    private String title;
    private String description;

    public Exercise(String title, String description, TrainingMethodology trainingMethodology, ExerciseFormat exerciseFormat) {
        validateTitle(title);
        validateDescription(description);
        validateTrainingMethod(trainingMethodology);
        this.trainingMethodology = trainingMethodology;
        validateExerciseFormatReference(exerciseFormat);
        this.title = title;
        this.description = description;
        this.exerciseFormat = exerciseFormat;
    }

    public String getTitle() {
        return title;
    }

    public void updateTitle(String title) {
        validateTitle(title);
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void updateDescription(String description) {
        validateDescription(description);
        this.description = description;
    }

    public ExerciseFormat getExerciseFormat() {
        return this.exerciseFormat;
    }

    private void validateTitle(String title) {
        if (title.isBlank()) {
            throw new IllegalArgumentException("Invalid title, cannot be null or empty");
        } else if (title.length() > 256) {
            throw new IllegalArgumentException("Title can have maximum 256 characters");
        }
    }

    private void validateDescription(String description) {
        if (description.length() > 3000) {
            throw new IllegalArgumentException("Description can have maximum 3000 characters");
        }
    }

    private void validateTrainingMethod(TrainingMethodology method) {
        if (method == null) throw new IllegalArgumentException("Method must not be null");
    }

    private void validateExerciseFormatReference(ExerciseFormat exerciseFormat) {
        if (exerciseFormat == null) {
            throw new IllegalArgumentException("Exercise must have one exercise format");
        } else if (this.trainingMethodology != exerciseFormat.getTrainingMethodology()) {
            throw new IllegalArgumentException("This exercise format cannot assign to any exercises belong to " + this.trainingMethodology.toString() + " methodology.");
        }
    }
}

package vn.ktt.ear_training_system.domain;

import java.util.ArrayList;
import java.util.Objects;

public class Exercise {
    private TrainingMethodology trainingMethodology;
    private ArrayList<ExerciseFormat> exerciseFormats;
    private String title;
    private String description;
    private Integer repetitions;
    private static final Integer INFINITE_REPETITIONS = Integer.MAX_VALUE;

    public Exercise(TrainingMethodology trainingMethodology, String title, String description, Integer repetitions, ArrayList<ExerciseFormat> exerciseFormats) {
        updateTitle(title);
        updateDescription(description);
        updateTrainingMethodology(trainingMethodology);
        updateExerciseFormats(exerciseFormats);
        updateRepetitions(repetitions);
    }

    public String getTitle() {
        return title;
    }

    public void updateTitle(String title) {
        validateTitle(title);
        this.title = title;
    }

    private void updateTrainingMethodology(TrainingMethodology methodology) {
        validateTrainingMethod(methodology);
        this.trainingMethodology = methodology;
    }

    public void updateRepetitions(Integer newRepetition) {
        validateRepetition(newRepetition);
        this.repetitions = Objects.requireNonNullElse(repetitions, INFINITE_REPETITIONS);
    }

    public String getDescription() {
        return description;
    }

    public void updateDescription(String description) {
        validateDescription(description);
        this.description = description;
    }

    public void updateExerciseFormats(ArrayList<ExerciseFormat> exerciseFormats) {
        validateExerciseFormatList(exerciseFormats);
        this.exerciseFormats = exerciseFormats;
    }

    public ArrayList<ExerciseFormat> getExerciseFormats() {
        return this.exerciseFormats;
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

    private void validateExerciseFormatList(ArrayList<ExerciseFormat> exerciseFormats) {
        if (exerciseFormats.isEmpty()) {
            throw new IllegalArgumentException("Exercise must have at least one exercise format");
        }
        for (var exerciseFormat : exerciseFormats) {
            if (exerciseFormat.getTrainingMethodology() != this.trainingMethodology) {
                throw new IllegalArgumentException("There is one exercise format that does not belong to the same methodology as the exercise");
            }
        }
    }

    private void validateRepetition(Integer repetitions) {
        if (repetitions == null) return;
        if (repetitions < 1) {
            throw new IllegalArgumentException("Invalid repetitions, must be a positive number");
        }
        if (repetitions > 10) {
            throw new IllegalArgumentException("Can reach maximum 10 reps");
        }
    }
}

package vn.ktt.ear_training_system.domain;

import lombok.Getter;

import java.util.*;

@Getter
public class Exercise {
    private final UUID exerciseId;
    private TrainingMethodology trainingMethodology;
    private String title;
    private String description;
    private Integer repetitions;
    private final Integer rest;
    private List<ExerciseFormat> exerciseFormats;
    private static final Integer INFINITE_REPETITIONS = null;

    public Exercise(TrainingMethodology trainingMethodology, String title, String description, Integer repetitions, Integer rest, List<ExerciseFormat> exerciseFormats) {
        this(null, trainingMethodology, title, description, repetitions, rest, exerciseFormats);
    }

    public Exercise(UUID exerciseId, TrainingMethodology trainingMethodology, String title, String description, Integer repetitions, Integer rest, List<ExerciseFormat> exerciseFormats) {
        this.exerciseId = exerciseId;
        updateTitle(title);
        updateDescription(description);
        updateTrainingMethodology(trainingMethodology);
        updateExerciseFormats(exerciseFormats);
        updateRepetitions(repetitions);
        this.rest = rest;
    }

    public String getExerciseId() {
        return exerciseId == null ? null : exerciseId.toString();
    }

    public UUID getExerciseUuid() {
        return exerciseId;
    }

    public String getTrainingMethodology() {
        return this.trainingMethodology.toString();
    }

    public TrainingMethodology getTrainingMethodologyEnum() {
        return this.trainingMethodology;
    }

    public List<ExerciseFormat> getExerciseFormats() {
        return Collections.unmodifiableList(this.exerciseFormats);
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
        this.repetitions = Objects.requireNonNullElse(newRepetition, INFINITE_REPETITIONS);
    }

    public void updateDescription(String description) {
        validateDescription(description);
        this.description = description;
    }

    public void updateExerciseFormats(List<ExerciseFormat> exerciseFormats) {
        validateExerciseFormatList(exerciseFormats);
        this.exerciseFormats = new ArrayList<>(exerciseFormats);
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

    private void validateExerciseFormatList(List<ExerciseFormat> exerciseFormats) {
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

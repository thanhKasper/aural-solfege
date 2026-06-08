package vn.ktt.ear_training_system.domain;

import lombok.Getter;

import java.util.*;

@Getter
public class Exercise {
    private final UUID exerciseId;
    private TrainingMethodology trainingMethodology;
    private String title;
    private String description;
    private boolean loop;
    private int repetitions;
    private int rest;
    private List<ExerciseFormat> exerciseFormats;

    public Exercise(TrainingMethodology trainingMethodology, String title, String description, boolean loop, int repetitions, int rest, List<ExerciseFormat> exerciseFormats) {
        this(null, trainingMethodology, title, description, loop, repetitions, rest, exerciseFormats);
    }

    public Exercise(UUID exerciseId, TrainingMethodology trainingMethodology, String title, String description, boolean loop, int repetitions, int rest, List<ExerciseFormat> exerciseFormats) {
        this.exerciseId = exerciseId;
        assignTrainingMethodology(trainingMethodology);
        rename(title);
        rephraseDescription(description);
        replaceExerciseFormats(exerciseFormats);
        changeRepetitions(loop, repetitions);
        changeRestDuration(rest);
    }

    public List<ExerciseFormat> getExerciseFormats() {
        return Collections.unmodifiableList(exerciseFormats);
    }

    public void rename(String title) {
        validateTitle(title);
        this.title = title;
    }

    public void rephraseDescription(String description) {
        validateDescription(description);
        this.description = description;
    }

    public void changeRepetitions(boolean loop, int repetitions) {
        if (!loop) {
            validateRepetition(repetitions);
        }
        this.loop = loop;
        this.repetitions = loop ? 0 : repetitions;
    }

    public void changeRestDuration(int rest) {
        validateRest(rest);
        this.rest = rest;
    }

    public void addFormat(ExerciseFormat format) {
        Objects.requireNonNull(format, "Exercise format must not be null");
        var newList = new ArrayList<>(this.exerciseFormats);
        newList.add(format);
        this.exerciseFormats = newList;
    }

    public void removeFormat(ExerciseFormat format) {
        var newList = new ArrayList<>(this.exerciseFormats);
        if (!newList.remove(format)) {
            return;
        }
        if (newList.isEmpty()) {
            throw new IllegalArgumentException("Exercise must have at least one exercise format");
        }
        this.exerciseFormats = newList;
    }

    private void assignTrainingMethodology(TrainingMethodology methodology) {
        if (methodology == null) throw new IllegalArgumentException("Method must not be null");
        this.trainingMethodology = methodology;
    }

    private void replaceExerciseFormats(List<ExerciseFormat> exerciseFormats) {
        if (exerciseFormats == null || exerciseFormats.isEmpty()) {
            throw new IllegalArgumentException("Exercise must have at least one exercise format");
        }
        this.exerciseFormats = new ArrayList<>(exerciseFormats);
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Invalid title, cannot be null or empty");
        }
        if (title.length() > 256) {
            throw new IllegalArgumentException("Title can have maximum 256 characters");
        }
    }

    private void validateDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Description must not be null");
        }
        if (description.length() > 3000) {
            throw new IllegalArgumentException("Description can have maximum 3000 characters");
        }
    }

    private void validateRepetition(int repetitions) {
        if (repetitions < 1 || repetitions > 10) {
            throw new IllegalArgumentException("Repetitions must be between 1 and 10");
        }
    }

    private void validateRest(int rest) {
        if (rest < 0 || rest > 1800) {
            throw new IllegalArgumentException("Rest must be between 0 and 1800");
        }
    }
}

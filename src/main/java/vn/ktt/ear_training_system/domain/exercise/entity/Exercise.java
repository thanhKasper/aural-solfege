package vn.ktt.ear_training_system.domain.exercise.entity;

import lombok.Getter;
import vn.ktt.ear_training_system.domain.exercise.value_object.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;
import vn.ktt.ear_training_system.domain.exercise.value_object.TrainingMethodology;

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
    private List<ExerciseActivity> exerciseActivities;

    private Exercise(UUID exerciseId, TrainingMethodology trainingMethodology, String title, String description, boolean loop, int repetitions, int rest, List<ExerciseActivity> exerciseActivities) {
        this.exerciseId = exerciseId;
        assignTrainingMethodology(trainingMethodology);
        rename(title);
        rephraseDescription(description);
        replaceExerciseActivities(exerciseActivities);
        validateActivityPositions(this.exerciseActivities);
        changeRepetitions(loop, repetitions);
        changeRestDuration(rest);
    }

    public static Exercise create(TrainingMethodology trainingMethodology, String title, String description, boolean loop, int repetitions, int rest, List<ExerciseActivity> exerciseActivities) {
        return new Exercise(null, trainingMethodology, title, description, loop, repetitions, rest, exerciseActivities);
    }

    public static Exercise reconstruct(UUID exerciseId, TrainingMethodology trainingMethodology, String title, String description, boolean loop, int repetitions, int rest, List<ExerciseActivity> exerciseActivities) {
        return new Exercise(exerciseId, trainingMethodology, title, description, loop, repetitions, rest, exerciseActivities);
    }

    public List<ExerciseActivity> getExerciseActivities() {
        return Collections.unmodifiableList(exerciseActivities);
    }

    public List<String> getIntervalNames() {
        return exerciseActivities.stream()
                .flatMap(a -> a.getIntervals().stream())
                .map(MusicalInterval::name)
                .distinct()
                .toList();
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

    public void replaceActivities(List<ExerciseActivity> newActivities) {
        if (newActivities == null || newActivities.isEmpty()) {
            throw new IllegalArgumentException("Exercise must have at least one activity");
        }
        validateActivityPositions(newActivities);
        this.exerciseActivities = new ArrayList<>(newActivities);
    }

    private void assignTrainingMethodology(TrainingMethodology methodology) {
        if (methodology == null) throw new IllegalArgumentException("Method must not be null");
        this.trainingMethodology = methodology;
    }

    private void validateActivityPositions(List<ExerciseActivity> activities) {
        var positions = activities.stream()
                .map(ExerciseActivity::getPosition)
                .sorted()
                .toList();
        for (int i = 0; i < positions.size(); i++) {
            if (positions.get(i) != i) {
                throw new IllegalArgumentException("Activity positions must be unique and consecutive starting from 0");
            }
        }
    }

    private void replaceExerciseActivities(List<ExerciseActivity> exerciseActivities) {
        if (exerciseActivities == null || exerciseActivities.isEmpty()) {
            throw new IllegalArgumentException("Exercise must have at least one exercise format");
        }
        this.exerciseActivities = new ArrayList<>(exerciseActivities);
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

package vn.ktt.ear_training_system.domain.exercise.entity;

import lombok.Getter;
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

    public void addActivity(ExerciseActivity format) {
        Objects.requireNonNull(format, "Exercise format must not be null");
        var newList = new ArrayList<>(this.exerciseActivities);
        newList.add(format);
        this.exerciseActivities = newList;
    }

    public void removeActivity(ExerciseActivity format) {
        var newList = new ArrayList<>(this.exerciseActivities);
        if (!newList.remove(format)) {
            return;
        }
        if (newList.isEmpty()) {
            throw new IllegalArgumentException("Exercise must have at least one exercise format");
        }
        this.exerciseActivities = newList;
    }

    public void reorderActivity(int oldPosition, int newPosition) {
        if (oldPosition == newPosition) return;
        var list = new ArrayList<>(this.exerciseActivities);
        int oldIndex = findIndexByPosition(list, oldPosition);
        int newIndex = findIndexByPosition(list, newPosition);
        if (oldIndex < 0) throw new IllegalArgumentException("Activity at position " + oldPosition + " not found");
        if (newIndex < 0) throw new IllegalArgumentException("Activity at position " + newPosition + " not found");
        var activity = list.remove(oldIndex);
        list.add(newIndex, activity);
        reassignPositions(list);
        this.exerciseActivities = list;
    }

    private void assignTrainingMethodology(TrainingMethodology methodology) {
        if (methodology == null) throw new IllegalArgumentException("Method must not be null");
        this.trainingMethodology = methodology;
    }

    private int findIndexByPosition(List<ExerciseActivity> list, int position) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getPosition() == position) return i;
        }
        return -1;
    }

    private void reassignPositions(List<ExerciseActivity> list) {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).changePosition(i);
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

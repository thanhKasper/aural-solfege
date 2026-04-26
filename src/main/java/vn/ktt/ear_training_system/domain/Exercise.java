package vn.ktt.ear_training_system.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="exercise")
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="id")
    private String exerciseId;

    @Enumerated(EnumType.STRING)
    @Column(name="training_methodology")
    private TrainingMethodology trainingMethodology;
    @Column(name="title")
    private String title;
    @Column(name = "description")
    private String description;
    @Column(name="repetitions")
    private Integer repetitions;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "exercise_id")
    private List<ExerciseFormat> exerciseFormats;
    private static final Integer INFINITE_REPETITIONS = Integer.MAX_VALUE;

    public Exercise(String exerciseId, TrainingMethodology trainingMethodology, String title, String description, Integer repetitions, List<ExerciseFormat> exerciseFormats) {
        updateTitle(title);
        updateDescription(description);
        updateTrainingMethodology(trainingMethodology);
        updateExerciseFormats(exerciseFormats);
        updateRepetitions(repetitions);
        this.exerciseId = exerciseId;
    }

    protected Exercise() {}

    public String getExerciseId() {
        return this.exerciseId;
    }

    public String getTitle() {
        return title;
    }

    public String getTrainingMethodology() {
        return this.trainingMethodology.toString();
    }

    public int getRepetitions() {
        return this.repetitions;
    }

    public String getDescription() {
        return description;
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
        this.exerciseFormats = new ArrayList<>(exerciseFormats); // defensive copy
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

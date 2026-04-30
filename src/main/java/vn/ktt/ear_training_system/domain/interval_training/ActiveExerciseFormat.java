package vn.ktt.ear_training_system.domain.interval_training;

import jakarta.persistence.*;
import vn.ktt.ear_training_system.domain.ExerciseFormat;
import vn.ktt.ear_training_system.domain.TrainingMethodology;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "active_training")
public class ActiveExerciseFormat extends ExerciseFormat {
    @Column(name = "practice_intervals")
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<MusicalInterval> practiceIntervals;
    @Column(name = "exercise_amounts")
    private int exerciseAmounts;
    private static final int PRACTICE_PER_INTERVAL = 5;

    public ActiveExerciseFormat(TrainingMethodology trainingMethodology, List<MusicalInterval> intervals) {
        super(trainingMethodology);
        validateSelectedIntervals(intervals);
        this.practiceIntervals = new HashSet<>(intervals);
        exerciseAmounts = this.practiceIntervals.size() * PRACTICE_PER_INTERVAL;
    }

    public ActiveExerciseFormat() {}

    public List<MusicalInterval> getPracticeIntervals() {
        return practiceIntervals.stream().toList();
    }

    public int getExerciseAmounts() {
        return exerciseAmounts;
    }

    public void validateSelectedIntervals(List<MusicalInterval> intervals) {
        if (intervals.isEmpty()) {
            throw new IllegalArgumentException("Interval must be selected");
        } else if (intervals.size() < 2) {
            throw new IllegalArgumentException("Choose at least two intervals to continue");
        }
    }
}

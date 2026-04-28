package vn.ktt.ear_training_system.domain.interval_training;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ActiveExerciseFormat {
    private final Set<MusicalInterval> practiceIntervals;
    private final int exerciseAmounts;
    private static final int PRACTICE_PER_INTERVAL = 5;

    public ActiveExerciseFormat(List<MusicalInterval> intervals) {
        validateSelectedIntervals(intervals);
        this.practiceIntervals = new HashSet<>(intervals);
        exerciseAmounts = this.practiceIntervals.size() * PRACTICE_PER_INTERVAL;
    }

    public Set<MusicalInterval> getPracticeIntervals() {
        return practiceIntervals;
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

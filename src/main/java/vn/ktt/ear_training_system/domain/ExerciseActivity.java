package vn.ktt.ear_training_system.domain;

import lombok.Getter;

@Getter
public abstract class ExerciseActivity {
    private final int position;
    public ExerciseActivity(int position) {
        this.position = position;
    }

    void validatePosition(int position) {
        if (position < 0) {
            throw new IllegalArgumentException("Position cannot be less than 0");
        }
    }
}

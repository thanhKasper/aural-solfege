package vn.ktt.ear_training_system.domain.exercise.entity;

import lombok.Getter;

@Getter
public abstract class ExerciseActivity {
    private int position;
    public ExerciseActivity(int position) {
        changePosition(position);
    }

    public void changePosition(int position) {
        validatePosition(position);
        this.position = position;
    }

    private void validatePosition(int position) {
        if (position < 0) {
            throw new IllegalArgumentException("Position cannot be less than 0");
        }
    }
}

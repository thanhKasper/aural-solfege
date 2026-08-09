package vn.ktt.ear_training_system.application.dtos.exercise_activities;

public sealed interface ExerciseActivityDTO
        permits SingleIntervalExerciseActivityDTO {
    Integer position();
    ExerciseActivityType type();
}

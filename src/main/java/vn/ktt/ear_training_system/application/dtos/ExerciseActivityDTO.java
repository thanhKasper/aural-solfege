package vn.ktt.ear_training_system.application.dtos;

public sealed interface ExerciseActivityDTO
        permits SingleIntervalExerciseActivityDTO {
    Integer position();
    ExerciseActivityType type();
}

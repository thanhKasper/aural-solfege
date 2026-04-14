package vn.ktt.ear_training_system.application.dtos;

public sealed interface ExerciseFormatDTO
        permits PassiveExerciseFormatDTO {
    String type();
}

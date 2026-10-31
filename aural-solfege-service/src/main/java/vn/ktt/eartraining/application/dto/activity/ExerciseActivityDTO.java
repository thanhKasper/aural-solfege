package vn.ktt.eartraining.application.dto.activity;

public sealed interface ExerciseActivityDTO
        permits SingleIntervalExerciseActivityDTO, IntervalSoundComparisonExerciseActivityDTO {
    Integer position();
    ExerciseActivityType type();
}

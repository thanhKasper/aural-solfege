package vn.ktt.eartraining.application.dto.activity;

public sealed interface ExerciseActivityDTO
        permits CoolDownRestActivityDTO, SingleIntervalExerciseActivityDTO, IntervalSoundComparisonExerciseActivityDTO {
    Integer position();
    ExerciseActivityType type();
}

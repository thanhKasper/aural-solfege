package vn.ktt.ear_training_system.application.dtos.exercise_activities;

import vn.ktt.ear_training_system.application.dtos.ExerciseActivityType;
import vn.ktt.ear_training_system.application.dtos.SingleIntervalExerciseActivityDTO;

public sealed interface ExerciseActivityDTO
        permits SingleIntervalExerciseActivityDTO {
    Integer position();
    ExerciseActivityType type();
}

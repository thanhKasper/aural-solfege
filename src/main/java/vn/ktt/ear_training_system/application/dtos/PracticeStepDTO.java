package vn.ktt.ear_training_system.application.dtos;

public sealed interface PracticeStepDTO
        permits ListenIntervalStepDTO {
    int stepNumber();
    int activityPosition();
    PracticeStepType stepType();
    String status();
}

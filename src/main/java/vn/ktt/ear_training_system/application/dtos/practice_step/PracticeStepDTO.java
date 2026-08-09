package vn.ktt.ear_training_system.application.dtos.practice_step;

public sealed interface PracticeStepDTO
        permits ListenIntervalStepDTO {
    int activityPosition();
    String status();
}

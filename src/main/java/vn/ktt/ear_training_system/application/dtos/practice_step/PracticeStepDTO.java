package vn.ktt.ear_training_system.application.dtos.practice_step;

public sealed interface PracticeStepDTO
        permits ListenIntervalStepDTO, CoolDownStepDTO {
    int activityPosition();
    String status();
}

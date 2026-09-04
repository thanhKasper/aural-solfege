package vn.ktt.ear_training_system.application.dtos.practice_step;

public sealed interface PracticeStepDTO
        permits ListenIntervalStepDTO, CoolDownStepDTO, IntervalSoundComparisonStepDTO {
    int activityPosition();
    String status();
    void setActivityPosition(int activityPosition);
    void setStatus(String status);
}

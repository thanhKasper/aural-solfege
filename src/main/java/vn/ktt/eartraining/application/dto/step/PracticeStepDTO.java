package vn.ktt.eartraining.application.dto.step;

public sealed interface PracticeStepDTO
        permits ListenIntervalStepDTO, CoolDownStepDTO, IntervalSoundComparisonStepDTO {
    int activityPosition();
    String status();
    void setActivityPosition(int activityPosition);
    void setStatus(String status);
}

package vn.ktt.eartraining.application.dto.step;

public sealed interface PracticeStepDTO
        permits ListenIntervalStepDTO, CoolDownStepDTO, IntervalDistanceComparisonStepDTO {
    int activityPosition();
    String status();
    void setActivityPosition(int activityPosition);
    void setStatus(String status);
}

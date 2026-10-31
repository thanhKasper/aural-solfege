package vn.ktt.eartraining.application.dto.step;

public sealed interface PracticeStepDTO
        permits ListenIntervalStepDTO, CoolDownStepDTO, IntervalSoundComparisonStepDTO {
}

package vn.ktt.eartraining.application.dto;

import vn.ktt.eartraining.application.dto.step.PracticeStepDTO;

import java.util.UUID;

public record SessionStepDTO(
        Metadata metadata,
        PracticeStepDTO currentStep
) {
    public record Metadata(
            UUID sessionId,
            int totalSteps,
            int currentStepIndex,
            int repetitions,
            boolean isLoop,
            int stepsPerRepetition,
            boolean hasNext
    ) {}
}

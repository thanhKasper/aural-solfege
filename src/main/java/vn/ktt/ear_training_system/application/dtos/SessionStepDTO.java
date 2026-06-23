package vn.ktt.ear_training_system.application.dtos;

import java.util.UUID;

public record SessionStepDTO(
        Metadata metadata,
        PracticeStepDTO currentStep
) {
    public record Metadata(
            UUID sessionId,
            int totalSteps,
            int currentStepIndex,
            boolean hasNext
    ) {}
}

package vn.ktt.ear_training_system.application.dtos;

import vn.ktt.ear_training_system.application.dtos.practice_step.PracticeStepDTO;

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

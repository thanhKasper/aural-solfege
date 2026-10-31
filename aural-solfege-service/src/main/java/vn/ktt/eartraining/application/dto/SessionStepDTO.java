package vn.ktt.eartraining.application.dto;

import vn.ktt.eartraining.application.dto.step.PracticeStepDTO;

import java.util.UUID;

public record SessionStepDTO(
        Metadata metadata,
        PracticeStepDTO currentStep
) {
    /**
     * What the front end needs to draw the activity map: one item per step group (activity), repeated
     * per repetition, with a rest item between repetitions. The rest duration is the exercise's {@code rest}
     * (the group labels come from the exercise too); the live rest step also carries it. With
     * {@code stride = groupsPerRepetition + (exercise.rest > 0 ? 1 : 0)}, an item sits at
     * {@code repetition * stride + groupPosition} (the rest at {@code repetition * stride + groupsPerRepetition}),
     * and the current position is {@code currentRepetition * stride + currentGroupPosition}.
     * {@code currentGroupPosition} is the position of the active group among the groups of its repetition:
     * the activities in order, then the rest, which sits at {@code groupsPerRepetition}.
     */
    public record Metadata(
            UUID sessionId,
            UUID exerciseId,
            int totalSteps,
            int currentStepIndex,
            int repetitions,
            int currentRepetition,
            int currentGroupPosition,
            int groupsPerRepetition,
            boolean hasNext
    ) {}
}

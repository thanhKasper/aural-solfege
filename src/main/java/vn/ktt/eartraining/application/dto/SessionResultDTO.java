package vn.ktt.eartraining.application.dto;

public record SessionResultDTO(
        int totalSteps,
        int completedSteps,
        long durationSeconds
) {
}

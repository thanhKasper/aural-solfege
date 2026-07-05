package vn.ktt.ear_training_system.application.dtos;

public record SessionResultDTO(
        int totalSteps,
        int completedSteps,
        long durationSeconds
) {
}

package vn.ktt.ear_training_system.infrastructure.dto;

import vn.ktt.ear_training_system.application.dtos.practice_step.PracticeStepDTO;
import vn.ktt.ear_training_system.application.dtos.SessionStepDTO;

public record PracticeStepResponse(
        SessionStepDTO.Metadata metadata,
        PracticeStepDTO currentStep
) {}

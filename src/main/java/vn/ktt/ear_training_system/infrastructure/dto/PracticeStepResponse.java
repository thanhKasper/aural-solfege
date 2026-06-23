package vn.ktt.ear_training_system.infrastructure.dto;

import vn.ktt.ear_training_system.application.dtos.PracticeStepDTO;
import vn.ktt.ear_training_system.application.dtos.SessionStepDTO;

public record PracticeStepResponse(
        SessionStepDTO.Metadata metadata,
        PracticeStepDTO currentStep,
        ApiCallSpec apiCall
) {}

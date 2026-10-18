package vn.ktt.eartraining.infrastructure.dto;

import vn.ktt.eartraining.application.dto.step.PracticeStepDTO;
import vn.ktt.eartraining.application.dto.SessionStepDTO;

public record PracticeStepResponse(
        SessionStepDTO.Metadata metadata,
        PracticeStepDTO currentStep
) {}

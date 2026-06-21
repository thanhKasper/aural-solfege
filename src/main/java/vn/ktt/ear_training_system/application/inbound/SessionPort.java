package vn.ktt.ear_training_system.application.inbound;

import vn.ktt.ear_training_system.application.dtos.PracticeStepResponseDTO;

import java.util.UUID;

public interface SessionPort {
    PracticeStepResponseDTO startSession(UUID exerciseId);
    PracticeStepResponseDTO advanceToNextStep(UUID sessionId);
}

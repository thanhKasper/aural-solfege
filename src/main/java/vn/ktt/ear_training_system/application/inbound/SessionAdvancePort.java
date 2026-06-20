package vn.ktt.ear_training_system.application.inbound;

import vn.ktt.ear_training_system.application.dtos.PracticeStepDTO;

import java.util.UUID;

public interface SessionAdvancePort {
    PracticeStepDTO advanceToNextStep(UUID sessionId);
}

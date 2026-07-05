package vn.ktt.ear_training_system.application.inbound;

import vn.ktt.ear_training_system.application.dtos.SessionResultDTO;
import vn.ktt.ear_training_system.application.dtos.SessionStepDTO;

import java.util.UUID;

public interface SessionPort {
    SessionStepDTO startSession(UUID exerciseId);
    SessionStepDTO advanceToNextStep(UUID sessionId);
    SessionResultDTO concludeSession(UUID sessionId);
    SessionResultDTO getSessionResult(UUID sessionId);
}

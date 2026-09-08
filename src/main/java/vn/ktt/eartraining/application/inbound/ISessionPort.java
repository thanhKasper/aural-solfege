package vn.ktt.eartraining.application.inbound;

import vn.ktt.eartraining.application.dto.SessionResultDTO;
import vn.ktt.eartraining.application.dto.SessionStepDTO;

import java.util.UUID;

public interface ISessionPort {
    SessionStepDTO startSession(UUID exerciseId);
    SessionStepDTO advanceToNextStep(UUID sessionId);
    SessionResultDTO concludeSession(UUID sessionId);
}

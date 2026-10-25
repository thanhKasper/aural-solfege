package vn.ktt.eartraining.application.inbound;

import vn.ktt.eartraining.application.dto.SessionDTO;
import vn.ktt.eartraining.application.dto.SessionResultDTO;
import vn.ktt.eartraining.application.dto.SessionStepDTO;

import java.util.List;
import java.util.UUID;

public interface ISessionPort {
    SessionDTO startSession(UUID exerciseId);
    SessionStepDTO advanceToNextStep(UUID sessionId);
    SessionResultDTO concludeSession(UUID sessionId);
    List<SessionDTO> getAvailableSessions(UUID exerciseId);
}

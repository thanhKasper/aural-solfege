package vn.ktt.ear_training_system.application;

import org.springframework.stereotype.Service;
import vn.ktt.ear_training_system.application.dtos.PracticeStepDTO;
import vn.ktt.ear_training_system.application.inbound.SessionAdvancePort;
import vn.ktt.ear_training_system.application.services.StepMapper;
import vn.ktt.ear_training_system.domain.practice_session.repository.IPracticeSessionRepository;

import java.util.UUID;

@Service
public class AdvanceSessionUseCase implements SessionAdvancePort {
    private final IPracticeSessionRepository sessionRepository;
    private final StepMapper stepMapper;

    public AdvanceSessionUseCase(IPracticeSessionRepository sessionRepository, StepMapper stepMapper) {
        this.sessionRepository = sessionRepository;
        this.stepMapper = stepMapper;
    }

    @Override
    public PracticeStepDTO advanceToNextStep(UUID sessionId) {
        var session = sessionRepository.getSessionById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        session.completeCurrentStep();

        if (!session.isNextStepAvailable()) {
            session.complete();
            sessionRepository.saveSession(session);
            throw new IllegalStateException("Session has been completed");
        }

        session.advanceToNextStep();
        session = sessionRepository.saveSession(session);
        return stepMapper.toDto(session.getCurrentStep());
    }
}

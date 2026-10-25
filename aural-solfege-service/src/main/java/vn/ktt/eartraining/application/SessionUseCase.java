package vn.ktt.eartraining.application;

import org.springframework.stereotype.Service;
import vn.ktt.eartraining.application.access.NotFoundException;
import vn.ktt.eartraining.application.dto.SessionDTO;
import vn.ktt.eartraining.application.dto.SessionResultDTO;
import vn.ktt.eartraining.application.dto.SessionStepDTO;
import vn.ktt.eartraining.application.inbound.ISessionPort;
import vn.ktt.eartraining.application.mapper.SessionMapper;
import vn.ktt.eartraining.domain.exercise.repository.IExerciseRepository;
import vn.ktt.eartraining.domain.session.entity.PracticeSession;
import vn.ktt.eartraining.domain.session.repository.IPracticeSessionRepository;
import vn.ktt.eartraining.domain.session.service.StepGenerationService;
import vn.ktt.eartraining.domain.session.valueobject.SessionStatus;
import vn.ktt.eartraining.domain.session.valueobject.StepStatus;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class SessionUseCase implements ISessionPort {
    private final IExerciseRepository exerciseRepository;
    private final IPracticeSessionRepository sessionRepository;
    private final StepGenerationService stepGenerationService;
    private final SessionMapper sessionMapper;

    public SessionUseCase(IExerciseRepository exerciseRepository,
                          IPracticeSessionRepository sessionRepository,
                          StepGenerationService stepGenerationService,
                          SessionMapper sessionMapper) {
        this.exerciseRepository = exerciseRepository;
        this.sessionRepository = sessionRepository;
        this.stepGenerationService = stepGenerationService;
        this.sessionMapper = sessionMapper;
    }

    @Override
    public SessionDTO startSession(UUID exerciseId) {
        var existing = sessionRepository.findByExercise(exerciseId);
        var exercise = exerciseRepository.getExerciseById(exerciseId.toString());

        if (existing.isPresent() && existing.get().getStatus() == SessionStatus.IN_PROGRESS) {
            return sessionMapper.mapToSessionDTO(existing.get());
        }

        var definitions = stepGenerationService.generate(exercise.getExerciseActivities(), exercise.getRepetitions());
        var session = PracticeSession.create(exerciseId, definitions);
        session.start();
        session = sessionRepository.saveSession(session);
        return sessionMapper.mapToSessionDTO(session);
    }

    @Override
    public SessionStepDTO advanceToNextStep(UUID sessionId) {
        var session = sessionRepository.getSessionById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found: " + sessionId));

        session.completeCurrentStep();

        if (!session.isNextStepAvailable()) {
            session.complete();
            sessionRepository.saveSession(session);
            throw new IllegalStateException("Session has been completed");
        }

        session.advanceToNextStep();
        session = sessionRepository.saveSession(session);

        var exercise = exerciseRepository.getExerciseById(session.getExerciseId().toString());
        var repetitions = exercise.getRepetitions();

        return sessionMapper.mapToSessionStepDTO(session, repetitions);
    }

    @Override
    public SessionResultDTO concludeSession(UUID sessionId) {
        var session = sessionRepository.getSessionById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found: " + sessionId));

        session.completeCurrentStep();
        session.conclude();
        sessionRepository.saveSession(session);

        long completed = session.getSteps().stream()
                .filter(s -> s.getStatus() == StepStatus.COMPLETED)
                .count();
        long duration = Duration.between(session.getStartedAt(), session.getCompletedAt()).getSeconds();

        return new SessionResultDTO(session.getSteps().size(), (int) completed, duration);
    }

    @Override
    public List<SessionDTO> getAvailableSessions(UUID exerciseId) {
        PracticeSession foundSession = sessionRepository.findByExercise(exerciseId).orElse(null);
        if (foundSession != null) {
            return List.of(sessionMapper.mapToSessionDTO(foundSession));
        }
        return List.of();
    }
}

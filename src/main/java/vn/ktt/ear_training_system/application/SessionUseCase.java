package vn.ktt.ear_training_system.application;

import org.springframework.stereotype.Service;
import vn.ktt.ear_training_system.application.dtos.SessionResultDTO;
import vn.ktt.ear_training_system.application.dtos.SessionStepDTO;
import vn.ktt.ear_training_system.application.inbound.SessionPort;
import vn.ktt.ear_training_system.application.mappers.SessionMapper;
import vn.ktt.ear_training_system.application.mappers.SessionResultMapper;
import vn.ktt.ear_training_system.domain.exercise.repository.IExerciseRepository;
import vn.ktt.ear_training_system.domain.guard.ExerciseModificationGuard;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeSession;
import vn.ktt.ear_training_system.domain.practice_session.repository.IPracticeSessionRepository;
import vn.ktt.ear_training_system.domain.practice_session.service.StepGenerationService;
import vn.ktt.ear_training_system.domain.practice_session.value_object.SessionStatus;

import java.util.UUID;

@Service
public class SessionUseCase implements SessionPort {
    private final IExerciseRepository exerciseRepository;
    private final IPracticeSessionRepository sessionRepository;
    private final StepGenerationService stepGenerationService;
    private final ExerciseModificationGuard guard;
    private final SessionMapper sessionMapper;
    private final SessionResultMapper sessionResultMapper;

    public SessionUseCase(IExerciseRepository exerciseRepository,
                          IPracticeSessionRepository sessionRepository,
                          StepGenerationService stepGenerationService,
                          ExerciseModificationGuard guard,
                          SessionMapper sessionMapper,
                          SessionResultMapper sessionResultMapper) {
        this.exerciseRepository = exerciseRepository;
        this.sessionRepository = sessionRepository;
        this.stepGenerationService = stepGenerationService;
        this.guard = guard;
        this.sessionMapper = sessionMapper;
        this.sessionResultMapper = sessionResultMapper;
    }

    @Override
    public SessionStepDTO startSession(UUID exerciseId) {
        var existing = sessionRepository.findByExercise(exerciseId);
        if (existing.isPresent()) {
            var session = existing.get();
            if (session.getStatus() == SessionStatus.ABANDONED) {
                session.resume();
                sessionRepository.saveSession(session);
            }
            return sessionMapper.toDto(session);
        }

        var exercise = exerciseRepository.getExerciseById(exerciseId.toString());
        guard.assertNoActiveSession(exercise);

        var definitions = stepGenerationService.generate(exercise.getExerciseActivities());
        var session = PracticeSession.create(exerciseId, definitions);
        session.start();
        session = sessionRepository.saveSession(session);
        return sessionMapper.toDto(session);
    }

    @Override
    public SessionStepDTO advanceToNextStep(UUID sessionId) {
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
        return sessionMapper.toDto(session);
    }

    @Override
    public SessionResultDTO getSessionResult(UUID sessionId) {
        var session = sessionRepository.getSessionById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (session.getResult() == null) {
            throw new IllegalStateException("Session has not been completed yet");
        }

        return sessionResultMapper.toDto(session.getResult());
    }
}

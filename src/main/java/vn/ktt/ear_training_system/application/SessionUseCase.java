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
        var exercise = exerciseRepository.getExerciseById(exerciseId.toString());
        int repetitions = exercise.getRepetitions();
        boolean isLoop = exercise.isLoop();

        if (existing.isPresent() && existing.get().getStatus() == SessionStatus.IN_PROGRESS) {
            return sessionMapper.toDto(existing.get(), repetitions, isLoop);
        }

        guard.assertNoActiveSession(exercise);

        var definitions = stepGenerationService.generate(exercise.getExerciseActivities(), exercise.getRepetitions());
        var session = PracticeSession.create(exerciseId, definitions);
        session.start();
        session = sessionRepository.saveSession(session);
        return sessionMapper.toDto(session, repetitions, isLoop);
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

        var exercise = exerciseRepository.getExerciseById(session.getExerciseId().toString());
        var repetitions = exercise.getRepetitions();
        var isLoop = exercise.isLoop();

        return sessionMapper.toDto(session, repetitions, isLoop);
    }

    @Override
    public SessionResultDTO concludeSession(UUID sessionId) {
        var session = sessionRepository.getSessionById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        session.conclude();
        session = sessionRepository.saveSession(session);
        return sessionResultMapper.toDto(session.getResult());
    }

    @Override
    public SessionResultDTO getSessionResult(UUID sessionId) {
        var session = sessionRepository.getSessionById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (session.getResult() == null) {
            throw new IllegalStateException("Session has not been completed yet");
        }

        var result = sessionResultMapper.toDto(session.getResult());
        sessionRepository.deleteSession(sessionId);
        return result;
    }
}

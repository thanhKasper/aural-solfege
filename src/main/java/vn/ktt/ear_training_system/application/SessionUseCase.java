package vn.ktt.ear_training_system.application;

import org.springframework.stereotype.Service;
import vn.ktt.ear_training_system.application.dtos.SessionResultDTO;
import vn.ktt.ear_training_system.application.dtos.SessionStepDTO;
import vn.ktt.ear_training_system.application.inbound.SessionPort;
import vn.ktt.ear_training_system.application.mappers.SessionMapper;
import vn.ktt.ear_training_system.domain.exercise.repository.IExerciseRepository;
import vn.ktt.ear_training_system.domain.guard.ExerciseModificationGuard;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeSession;
import vn.ktt.ear_training_system.domain.practice_session.repository.IPracticeSessionRepository;
import vn.ktt.ear_training_system.domain.practice_session.service.StepGenerationService;
import vn.ktt.ear_training_system.domain.practice_session.value_object.SessionStatus;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepStatus;

import java.time.Duration;
import java.util.UUID;

@Service
public class SessionUseCase implements SessionPort {
    private final IExerciseRepository exerciseRepository;
    private final IPracticeSessionRepository sessionRepository;
    private final StepGenerationService stepGenerationService;
    private final ExerciseModificationGuard guard;
    private final SessionMapper sessionMapper;

    public SessionUseCase(IExerciseRepository exerciseRepository,
                          IPracticeSessionRepository sessionRepository,
                          StepGenerationService stepGenerationService,
                          ExerciseModificationGuard guard,
                          SessionMapper sessionMapper) {
        this.exerciseRepository = exerciseRepository;
        this.sessionRepository = sessionRepository;
        this.stepGenerationService = stepGenerationService;
        this.guard = guard;
        this.sessionMapper = sessionMapper;
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

        session.completeCurrentStep();
        session.conclude();
        sessionRepository.saveSession(session);

        long completed = session.getSteps().stream()
                .filter(s -> s.getStatus() == StepStatus.COMPLETED)
                .count();
        long duration = Duration.between(session.getStartedAt(), session.getCompletedAt()).getSeconds();

        return new SessionResultDTO(session.getSteps().size(), (int) completed, duration);
    }
}

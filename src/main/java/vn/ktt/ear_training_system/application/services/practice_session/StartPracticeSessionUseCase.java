package vn.ktt.ear_training_system.application.services.practice_session;

import org.springframework.stereotype.Service;
import vn.ktt.ear_training_system.application.inbound.SessionStartPort;
import vn.ktt.ear_training_system.domain.exercise.repository.IExerciseRepository;
import vn.ktt.ear_training_system.domain.guard.ExerciseModificationGuard;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeSession;
import vn.ktt.ear_training_system.domain.practice_session.repository.IPracticeSessionRepository;
import vn.ktt.ear_training_system.domain.practice_session.service.StepGenerationService;
import vn.ktt.ear_training_system.domain.practice_session.value_object.SessionStatus;

import java.util.UUID;

@Service
public class StartPracticeSessionUseCase implements SessionStartPort {
    private final IExerciseRepository exerciseRepository;
    private final IPracticeSessionRepository sessionRepository;
    private final StepGenerationService stepGenerationService;
    private final ExerciseModificationGuard guard;

    public StartPracticeSessionUseCase(IExerciseRepository exerciseRepository,
                                       IPracticeSessionRepository sessionRepository,
                                       StepGenerationService stepGenerationService,
                                       ExerciseModificationGuard guard) {
        this.exerciseRepository = exerciseRepository;
        this.sessionRepository = sessionRepository;
        this.stepGenerationService = stepGenerationService;
        this.guard = guard;
    }

    @Override
    public PracticeSession startSession(UUID exerciseId) {
        var existing = sessionRepository.findByExercise(exerciseId);
        if (existing.isPresent()) {
            var session = existing.get();
            if (session.getStatus() == SessionStatus.ABANDONED) {
                session.resume();
                sessionRepository.saveSession(session);
            }
            return session;
        }

        var exercise = exerciseRepository.getExerciseById(exerciseId.toString());
        guard.assertNoActiveSession(exercise);

        var definitions = stepGenerationService.generate(exercise.getExerciseActivities());
        var session = PracticeSession.create(UUID.randomUUID(), exerciseId, definitions);
        session.start();
        sessionRepository.saveSession(session);
        return session;
    }
}

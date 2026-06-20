package vn.ktt.ear_training_system.application;

import org.springframework.stereotype.Service;
import vn.ktt.ear_training_system.application.dtos.PracticeStepDTO;
import vn.ktt.ear_training_system.application.inbound.SessionStartPort;
import vn.ktt.ear_training_system.application.services.StepMapper;
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
    private final StepMapper stepMapper;

    public StartPracticeSessionUseCase(IExerciseRepository exerciseRepository,
                                       IPracticeSessionRepository sessionRepository,
                                       StepGenerationService stepGenerationService,
                                       ExerciseModificationGuard guard,
                                       StepMapper stepMapper) {
        this.exerciseRepository = exerciseRepository;
        this.sessionRepository = sessionRepository;
        this.stepGenerationService = stepGenerationService;
        this.guard = guard;
        this.stepMapper = stepMapper;
    }

    @Override
    public PracticeStepDTO startSession(UUID exerciseId) {
        var existing = sessionRepository.findByExercise(exerciseId);
        if (existing.isPresent()) {
            var session = existing.get();
            if (session.getStatus() == SessionStatus.ABANDONED) {
                session.resume();
                sessionRepository.saveSession(session);
            }
            return stepMapper.toDto(session.getCurrentStep());
        }

        var exercise = exerciseRepository.getExerciseById(exerciseId.toString());
        guard.assertNoActiveSession(exercise);

        var definitions = stepGenerationService.generate(exercise.getExerciseActivities());
        var session = PracticeSession.create(exerciseId, definitions);
        session.start();
        session = sessionRepository.saveSession(session);
        return stepMapper.toDto(session.getCurrentStep());
    }
}

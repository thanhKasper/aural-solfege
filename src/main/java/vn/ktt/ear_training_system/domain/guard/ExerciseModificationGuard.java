package vn.ktt.ear_training_system.domain.guard;

import vn.ktt.ear_training_system.domain.exercise.entity.Exercise;
import vn.ktt.ear_training_system.domain.practice_session.repository.IPracticeSessionRepository;

public class ExerciseModificationGuard {
    private final IPracticeSessionRepository sessionRepository;

    public ExerciseModificationGuard(IPracticeSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public void assertModifiable(Exercise exercise) {
        if (sessionRepository.existsActiveSessionForExercise(exercise.getExerciseId())) {
            throw new IllegalStateException(
                    "Cannot modify exercise " + exercise.getExerciseId()
                            + " while active practice sessions exist");
        }
    }

    public void assertNoActiveSession(Exercise exercise) {
        if (sessionRepository.existsActiveSessionForExercise(exercise.getExerciseId())) {
            throw new IllegalStateException(
                    "Exercise " + exercise.getExerciseId()
                            + " already has an active practice session");
        }
    }
}

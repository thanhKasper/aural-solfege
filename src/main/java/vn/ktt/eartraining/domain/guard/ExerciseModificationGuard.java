package vn.ktt.eartraining.domain.guard;

import vn.ktt.eartraining.domain.exercise.entity.Exercise;
import vn.ktt.eartraining.domain.session.repository.IPracticeSessionRepository;

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

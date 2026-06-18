package vn.ktt.ear_training_system.domain.practice_session.repository;

import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeSession;

import java.util.Optional;
import java.util.UUID;

public interface IPracticeSessionRepository {
    void saveSession(PracticeSession session);
    Optional<PracticeSession> getSessionById(UUID sessionId);
    boolean existsActiveSessionForExercise(UUID exerciseId);
}

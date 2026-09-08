package vn.ktt.eartraining.domain.session.repository;

import vn.ktt.eartraining.domain.session.entity.PracticeSession;

import java.util.Optional;
import java.util.UUID;

public interface IPracticeSessionRepository {
    PracticeSession saveSession(PracticeSession session);
    Optional<PracticeSession> getSessionById(UUID sessionId);
    Optional<PracticeSession> findByExercise(UUID exerciseId);
    boolean existsActiveSessionForExercise(UUID exerciseId);
}

package vn.ktt.ear_training_system.application.inbound;

import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeSession;

import java.util.UUID;

public interface SessionStartPort {
    PracticeSession startSession(UUID exerciseId);
}

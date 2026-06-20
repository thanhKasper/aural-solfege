package vn.ktt.ear_training_system.application.inbound;

import vn.ktt.ear_training_system.application.dtos.PracticeStepDTO;

import java.util.UUID;

public interface SessionStartPort {
    PracticeStepDTO startSession(UUID exerciseId);
}

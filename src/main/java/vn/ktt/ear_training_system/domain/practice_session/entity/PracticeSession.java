package vn.ktt.ear_training_system.domain.practice_session.entity;

import lombok.Getter;
import vn.ktt.ear_training_system.domain.practice_session.value_object.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Getter
public class PracticeSession {
    private final UUID sessionId;
    private final UUID exerciseId;
    private SessionStatus status;
    private int currentStepIndex;
    private final List<PracticeStep> steps;
    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;
    private SessionResult result;

    public static PracticeSession create(UUID exerciseId, List<StepDefinition> definitions) {
        var steps = new ArrayList<PracticeStep>();
        for (int i = 0; i < definitions.size(); i++) {
            var def = definitions.get(i);
            steps.add(new PracticeStep(def.getActivityPosition(), def.getStepType(), def.getContext()));
        }
        return new PracticeSession(null, exerciseId, steps);
    }

    public static PracticeSession reconstruct(
            UUID sessionId,
            UUID exerciseId,
            SessionStatus status,
            int currentStepIndex,
            List<PracticeStep> steps,
            Instant createdAt,
            Instant startedAt,
            Instant completedAt,
            SessionResult result) {
        var session = new PracticeSession(sessionId, exerciseId, steps);
        session.status = status;
        session.currentStepIndex = currentStepIndex;
        session.createdAt = createdAt;
        session.startedAt = startedAt;
        session.completedAt = completedAt;
        session.result = result;
        return session;
    }

    private PracticeSession(UUID sessionId, UUID exerciseId, List<PracticeStep> steps) {
        this.sessionId = sessionId;
        this.exerciseId = Objects.requireNonNull(exerciseId);
        this.steps = new ArrayList<>(steps);
        this.status = SessionStatus.CREATED;
        this.currentStepIndex = 0;
        this.createdAt = Instant.now();
    }

    public void start() {
        assertStatus(SessionStatus.CREATED, "Only CREATED sessions can be started");
        this.status = SessionStatus.IN_PROGRESS;
        this.startedAt = Instant.now();
        if (!steps.isEmpty()) {
            steps.get(currentStepIndex).activate();
        }
    }

    public PracticeStep getCurrentStep() {
        assertStatus(SessionStatus.IN_PROGRESS, "Session is not in progress");
        return steps.get(currentStepIndex);
    }

    public void completeCurrentStep() {
        assertStatus(SessionStatus.IN_PROGRESS, "Session is not in progress");
        steps.get(currentStepIndex).markCompleted();
    }

    public void skipCurrentStep() {
        assertStatus(SessionStatus.IN_PROGRESS, "Session is not in progress");
        steps.get(currentStepIndex).skip();
    }

    public void advanceToNextStep() {
        assertStatus(SessionStatus.IN_PROGRESS, "Session is not in progress");
        if (!isSufficientToAdvance()) {
            throw new IllegalStateException("Current step is not complete");
        }
        if (!isNextStepAvailable()) {
            throw new IllegalStateException("No next step available");
        }
        currentStepIndex++;
        steps.get(currentStepIndex).activate();
    }

    public boolean isSufficientToAdvance() {
        return steps.get(currentStepIndex).getStatus() == StepStatus.COMPLETED;
    }

    public boolean isNextStepAvailable() {
        return currentStepIndex < steps.size() - 1;
    }

    public void complete() {
        assertStatus(SessionStatus.IN_PROGRESS, "Only IN_PROGRESS sessions can be completed");
        this.status = SessionStatus.COMPLETED;
        this.completedAt = Instant.now();
        computeResult();
    }

    public void abandon() {
        assertStatus(SessionStatus.IN_PROGRESS, "Only IN_PROGRESS sessions can be abandoned");
        this.status = SessionStatus.ABANDONED;
        this.completedAt = Instant.now();
    }

    public void resume() {
        assertStatus(SessionStatus.ABANDONED, "Only ABANDONED sessions can be resumed");
        this.status = SessionStatus.IN_PROGRESS;
        this.completedAt = null;
        steps.get(currentStepIndex).activate();
    }

    public int getCurrentActivityPosition() {
        return steps.get(currentStepIndex).getActivityPosition();
    }

    public List<PracticeStep> getStepsForActivity(int activityPosition) {
        return steps.stream()
                .filter(s -> s.getActivityPosition() == activityPosition)
                .toList();
    }

    public boolean isCurrentActivityCompleted() {
        return getStepsForActivity(getCurrentActivityPosition()).stream()
                .allMatch(s -> s.getStatus() == StepStatus.COMPLETED);
    }

    private void computeResult() {
        var completed = steps.stream()
                .filter(s -> s.getStatus() == StepStatus.COMPLETED)
                .count();
        this.result = new SessionResult(
                steps.size(),
                (int) completed,
                Duration.between(startedAt, completedAt)
        );
    }

    private void assertStatus(SessionStatus expected, String message) {
        if (this.status != expected) {
            throw new IllegalStateException(message + " (current: " + this.status + ")");
        }
    }
}

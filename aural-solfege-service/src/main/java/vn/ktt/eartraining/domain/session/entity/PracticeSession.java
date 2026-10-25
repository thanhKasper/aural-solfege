package vn.ktt.eartraining.domain.session.entity;

import lombok.Getter;
import vn.ktt.eartraining.domain.session.valueobject.*;

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

    /**
     * Wraps steps that were already laid out, normally by {@code PracticeSessionFactory}.
     * The session only protects what it holds: at least one step, ordered by repetition and group.
     */
    public static PracticeSession create(UUID exerciseId, List<PracticeStep> steps) {
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("A session needs at least one step");
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
            Instant completedAt) {
        var session = new PracticeSession(sessionId, exerciseId, steps);
        session.status = status;
        session.currentStepIndex = currentStepIndex;
        session.createdAt = createdAt;
        session.startedAt = startedAt;
        session.completedAt = completedAt;
        return session;
    }

    private PracticeSession(UUID sessionId, UUID exerciseId, List<PracticeStep> steps) {
        this.sessionId = sessionId;
        this.exerciseId = Objects.requireNonNull(exerciseId);
        assertOrdered(steps);
        this.steps = new ArrayList<>(steps);
        this.status = SessionStatus.CREATED;
        this.currentStepIndex = 0;
        this.createdAt = Instant.now();
    }

    private static void assertOrdered(List<PracticeStep> steps) {
        int group = 0;
        int repetitionIndex = 0;
        for (PracticeStep step : steps) {
            if (step.getStepGroup() < group || step.getRepetitionIndex() < repetitionIndex) {
                throw new IllegalArgumentException("Steps must be ordered by repetitionIndex and group");
            }
            group = step.getStepGroup();
            repetitionIndex = step.getRepetitionIndex();
        }
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
    }

    public void conclude() {
        assertStatus(SessionStatus.IN_PROGRESS, "Only IN_PROGRESS sessions can be concluded");
        this.status = SessionStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public int getCurrentStepGroup() {
        return steps.get(currentStepIndex).getStepGroup();
    }

    public List<PracticeStep> getStepsInGroup(int stepGroup) {
        return steps.stream()
                .filter(s -> s.getStepGroup() == stepGroup)
                .toList();
    }

    public boolean isCurrentGroupCompleted() {
        return getStepsInGroup(getCurrentStepGroup()).stream()
                .allMatch(s -> s.getStatus() == StepStatus.COMPLETED);
    }

    public List<PracticeStep> getStepsInRepetition(int repetitionIndex) {
        return steps.stream()
                .filter(s -> s.getRepetitionIndex() == repetitionIndex)
                .toList();
    }

    // view*: what the learner sees. Rests are part of the flow but are never counted here.
    // Everything else works on the raw step list, where a rest is a step like any other.

    /** Steps of the exercise, rests excluded. */
    public int viewTotalSteps() {
        return (int) steps.stream().filter(s -> !s.isRest()).count();
    }

    public int viewCompletedSteps() {
        return (int) steps.stream().filter(s -> !s.isRest() && s.getStatus() == StepStatus.COMPLETED).count();
    }

    /**
     * Number of exercise steps before the current one, rests excluded. On an exercise step this is
     * its own position; while resting it is the position of the step that follows the rest.
     */
    public int viewCurrentStepIndex() {
        return (int) steps.subList(0, currentStepIndex).stream().filter(s -> !s.isRest()).count();
    }

    /** How many times the exercise is played: the highest repetition among the steps, plus one. */
    public int getRepeat() {
        return steps.stream().mapToInt(PracticeStep::getRepetitionIndex).max().orElse(-1) + 1;
    }

    /**
     * Position of the step's group among the groups of its own repetition: the activities in
     * order, then the rest that follows the repetition (if any).
     */
    public int groupPositionInRepetition(PracticeStep step) {
        return steps.stream()
                .filter(s -> s.getRepetitionIndex() == step.getRepetitionIndex())
                .map(PracticeStep::getStepGroup)
                .distinct()
                .sorted()
                .toList()
                .indexOf(step.getStepGroup());
    }

    private void assertStatus(SessionStatus expected, String message) {
        if (this.status != expected) {
            throw new IllegalStateException(message + " (current: " + this.status + ")");
        }
    }
}

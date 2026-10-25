package vn.ktt.eartraining.domain.session.entity;

import org.junit.jupiter.api.Test;
import vn.ktt.eartraining.domain.exercise.valueobject.IntervalTexture;
import vn.ktt.eartraining.domain.exercise.valueobject.MusicalInterval;
import vn.ktt.eartraining.domain.session.valueobject.SessionStatus;
import vn.ktt.eartraining.domain.session.valueobject.StepStatus;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.CoolDownContext;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.ListenIntervalContext;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PracticeSessionTest {

    private static PracticeStep step(int group, int repetitionIndex) {
        return PracticeStep.planned(group, repetitionIndex, new CoolDownContext(1));
    }

    private static PracticeStep listen(int group, int repetitionIndex) {
        return PracticeStep.planned(group, repetitionIndex,
                new ListenIntervalContext(MusicalInterval.MAJOR_2ND, "UP", IntervalTexture.ASCENDING));
    }

    private static PracticeStep rest(int group, int repetitionIndex) {
        return PracticeStep.planned(group, repetitionIndex, new CoolDownContext(30));
    }

    /** Two repetitions of two exercise steps, with a rest between them. */
    private static PracticeSession sessionWithARest() {
        return PracticeSession.create(UUID.randomUUID(), List.of(
                listen(0, 0), listen(1, 0), rest(2, 0),
                listen(3, 1), listen(4, 1)));
    }

    @Test
    void restsAreNeverCounted() {
        var session = sessionWithARest();
        session.start();

        assertEquals(5, session.getSteps().size());
        assertEquals(4, session.viewTotalSteps());
        assertEquals(0, session.viewCompletedSteps());

        for (int i = 0; i < 3; i++) {
            session.completeCurrentStep();
            session.advanceToNextStep();
        }
        // the first two exercise steps and the rest are done; the rest does not count
        assertEquals(2, session.viewCompletedSteps());
    }

    @Test
    void currentStepPositionSkipsRestsAndPointsAtTheNextStepWhileResting() {
        var session = sessionWithARest();
        session.start();

        var positions = new java.util.ArrayList<Integer>();
        for (int i = 0; i < 5; i++) {
            positions.add(session.viewCurrentStepIndex());
            if (session.isNextStepAvailable()) {
                session.completeCurrentStep();
                session.advanceToNextStep();
            }
        }

        // steps: listen, listen, rest, listen, listen
        assertEquals(List.of(0, 1, 2, 2, 3), positions);
    }

    @Test
    void createRejectsNoSteps() {
        var id = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> PracticeSession.create(id, List.of()));
        assertThrows(IllegalArgumentException.class, () -> PracticeSession.create(id, null));
    }

    @Test
    void createRejectsStepsThatGoBackwards() {
        var id = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class,
                () -> PracticeSession.create(id, List.of(step(1, 0), step(0, 0))));
        assertThrows(IllegalArgumentException.class,
                () -> PracticeSession.create(id, List.of(step(0, 1), step(1, 0))));
    }

    @Test
    void reconstructRejectsStepsThatGoBackwards() {
        var id = UUID.randomUUID();
        var steps = List.of(step(2, 0), step(1, 0));
        assertThrows(IllegalArgumentException.class, () -> PracticeSession.reconstruct(
                UUID.randomUUID(), id, SessionStatus.CREATED, 0, steps, null, null, null));
    }

    @Test
    void repeatIsDerivedFromTheStepRepetitions() {
        var session = PracticeSession.create(UUID.randomUUID(),
                List.of(step(0, 0), step(1, 1), step(2, 2)));

        assertEquals(3, session.getRepeat());
    }

    @Test
    void groupLookupDoesNotMixRepetitions() {
        // repetition 0: group 0 (2 steps), group 1 (1 step); repetition 1: groups 2 and 3
        var session = PracticeSession.create(UUID.randomUUID(), List.of(
                step(0, 0), step(0, 0), step(1, 0),
                step(2, 1), step(2, 1), step(3, 1)));
        session.start();

        assertEquals(0, session.getCurrentStepGroup());
        assertEquals(2, session.getStepsInGroup(0).size());
        assertEquals(2, session.getStepsInGroup(2).size());
        assertEquals(1, session.getStepsInGroup(3).size());
        assertFalse(session.isCurrentGroupCompleted());

        session.completeCurrentStep();
        session.advanceToNextStep();
        session.completeCurrentStep();
        assertTrue(session.isCurrentGroupCompleted());
    }

    @Test
    void stepsAreGroupedByRepetition() {
        var session = PracticeSession.create(UUID.randomUUID(),
                List.of(step(0, 0), step(1, 0), step(2, 1)));

        assertEquals(2, session.getStepsInRepetition(0).size());
        assertEquals(1, session.getStepsInRepetition(1).size());
    }

    @Test
    void groupPositionRestartsInEachRepetition() {
        var session = PracticeSession.create(UUID.randomUUID(), List.of(
                step(0, 0), step(1, 0), step(2, 0),
                step(3, 1), step(4, 1)));

        var positions = session.getSteps().stream().map(session::groupPositionInRepetition).toList();

        assertEquals(List.of(0, 1, 2, 0, 1), positions);
    }

    @Test
    void reconstructKeepsStepStatuses() {
        var steps = List.of(
                PracticeStep.reconstruct(0, 0, StepStatus.COMPLETED, new CoolDownContext(1)),
                PracticeStep.reconstruct(1, 0, StepStatus.ACTIVE, new CoolDownContext(1)));

        var session = PracticeSession.reconstruct(
                UUID.randomUUID(), UUID.randomUUID(), SessionStatus.IN_PROGRESS, 1, steps, null, null, null);

        assertEquals(StepStatus.ACTIVE, session.getCurrentStep().getStatus());
    }
}

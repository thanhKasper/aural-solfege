package vn.ktt.eartraining.domain.session.factory;

import org.junit.jupiter.api.Test;
import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.exercise.valueobject.IntervalTexture;
import vn.ktt.eartraining.domain.exercise.valueobject.MusicalInterval;
import vn.ktt.eartraining.domain.exercise.valueobject.activity.SingleIntervalExerciseActivity;
import vn.ktt.eartraining.domain.session.entity.PracticeStep;
import vn.ktt.eartraining.domain.session.service.IStepGeneration;
import vn.ktt.eartraining.domain.session.service.SingleIntervalStepGeneration;
import vn.ktt.eartraining.domain.session.service.StepGenerationService;
import vn.ktt.eartraining.domain.session.valueobject.StepGroupBlueprint;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.CoolDownContext;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PracticeSessionFactoryTest {

    private final PracticeSessionFactory factory =
            new PracticeSessionFactory(new StepGenerationService(List.of(new SingleIntervalStepGeneration())));

    /** Two activities, each playing one interval up and down: 2 steps per activity. */
    private static List<ExerciseActivity> twoActivities() {
        return List.of(
                new SingleIntervalExerciseActivity(IntervalTexture.ASCENDING, List.of(MusicalInterval.MAJOR_2ND), 0),
                new SingleIntervalExerciseActivity(IntervalTexture.ASCENDING, List.of(MusicalInterval.PERFECT_5TH), 1));
    }

    private static boolean isRest(PracticeStep step) {
        return step.isRest();
    }

    @Test
    void putsARestBetweenRepetitionsButNotAfterTheLastOne() {
        var session = factory.create(UUID.randomUUID(), twoActivities(), 3, 60);

        assertEquals(3, session.getRepeat());
        assertEquals(14, session.getSteps().size());
        assertEquals(List.of(0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 6, 6, 7, 7),
                session.getSteps().stream().map(PracticeStep::getStepGroup).toList());
        assertEquals(List.of(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2),
                session.getSteps().stream().map(PracticeStep::getRepetitionIndex).toList());

        var rests = session.getSteps().stream().filter(PracticeSessionFactoryTest::isRest).toList();
        assertEquals(2, rests.size());
        assertEquals(new CoolDownContext(60), rests.get(0).getContext());
        assertEquals(List.of(0, 1), rests.stream().map(PracticeStep::getRepetitionIndex).toList());
    }

    @Test
    void restComesAfterTheActivitiesOfItsRepetition() {
        var session = factory.create(UUID.randomUUID(), twoActivities(), 2, 30);

        var positions = session.getSteps().stream().map(session::groupPositionInRepetition).toList();

        assertEquals(List.of(0, 0, 1, 1, 2, 0, 0, 1, 1), positions);
    }

    @Test
    void walkingTheWholeSessionKeepsTheCountsConsistent() {
        var session = factory.create(UUID.randomUUID(), twoActivities(), 3, 60);
        assertEquals(12, session.viewTotalSteps());

        int previousPosition = 0;
        int exerciseStepsSeen = 0;
        while (true) {
            int position = session.viewCurrentStepIndex();
            assertTrue(position >= previousPosition, "position must never go backwards");
            if (!session.getCurrentStep().isRest()) {
                assertEquals(exerciseStepsSeen, position);
                exerciseStepsSeen++;
            }
            previousPosition = position;

            session.completeCurrentStep();
            if (!session.isNextStepAvailable()) {
                break;
            }
            session.advanceToNextStep();
        }

        assertEquals(12, exerciseStepsSeen);
        assertEquals(12, session.viewCompletedSteps());
        assertFalse(session.getSteps().getLast().isRest());
    }

    @Test
    void zeroRestMeansNoRestSteps() {
        var session = factory.create(UUID.randomUUID(), twoActivities(), 3, 0);

        assertEquals(12, session.getSteps().size());
        assertEquals(0, session.getSteps().stream().filter(PracticeSessionFactoryTest::isRest).count());
    }

    @Test
    void aSingleRepetitionHasNoRest() {
        var session = factory.create(UUID.randomUUID(), twoActivities(), 1, 60);

        assertEquals(4, session.getSteps().size());
        assertEquals(0, session.getSteps().stream().filter(PracticeSessionFactoryTest::isRest).count());
    }

    @Test
    void rejectsAnActivityThatGeneratesARestStep() {
        IStepGeneration restGenerator = new IStepGeneration() {
            @Override
            public StepGroupBlueprint generate(ExerciseActivity activity) {
                return new StepGroupBlueprint(List.of(new CoolDownContext(5)));
            }

            @Override
            public Class<? extends ExerciseActivity> getKey() {
                return SingleIntervalExerciseActivity.class;
            }

            @Override
            public IStepGeneration getService() {
                return this;
            }
        };
        var strictFactory = new PracticeSessionFactory(new StepGenerationService(List.of(restGenerator)));
        var id = UUID.randomUUID();
        var activities = twoActivities();

        assertThrows(IllegalStateException.class, () -> strictFactory.create(id, activities, 2, 60));
        assertThrows(IllegalStateException.class, () -> strictFactory.create(id, activities, 1, 0));
    }

    @Test
    void rejectsInvalidInput() {
        var id = UUID.randomUUID();
        var activities = twoActivities();
        assertThrows(IllegalArgumentException.class, () -> factory.create(id, activities, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> factory.create(id, activities, 1, -1));
        assertThrows(IllegalArgumentException.class, () -> factory.create(id, List.of(), 1, 10));
    }
}

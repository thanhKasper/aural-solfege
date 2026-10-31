package vn.ktt.eartraining.domain.session.factory;

import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.session.entity.PracticeSession;
import vn.ktt.eartraining.domain.session.entity.PracticeStep;
import vn.ktt.eartraining.domain.session.service.StepGenerationService;
import vn.ktt.eartraining.domain.session.valueobject.StepGroupBlueprint;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.CoolDownContext;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.StepContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Lays out a whole practice session: the exercise's activities played once per repetition, with a
 * rest step between two repetitions (never after the last one). The layout rules live here so the
 * aggregate only has to protect the steps it is given.
 */
public class PracticeSessionFactory {
    private final StepGenerationService stepGenerationService;

    public PracticeSessionFactory(StepGenerationService stepGenerationService) {
        this.stepGenerationService = stepGenerationService;
    }

    /**
     * @param restBetweenRepetitionsInSecond rest between two repetitions; {@code 0} means no rest step
     */
    public PracticeSession create(UUID exerciseId,
                                  List<ExerciseActivity> activities,
                                  int repetitions,
                                  int restBetweenRepetitionsInSecond) {
        if (repetitions < 1) {
            throw new IllegalArgumentException("A session must repeat at least once");
        }
        if (restBetweenRepetitionsInSecond < 0) {
            throw new IllegalArgumentException("Rest between repetitions cannot be negative");
        }
        var pass = stepGenerationService.generate(activities);
        if (pass.isEmpty()) {
            throw new IllegalArgumentException("A session needs at least one activity");
        }
        assertNoRestInActivities(pass);

        var steps = new ArrayList<PracticeStep>();
        int group = 0;
        for (int repetitionIndex = 0; repetitionIndex < repetitions; repetitionIndex++) {
            for (var blueprint : pass) {
                for (var context : blueprint.steps()) {
                    steps.add(PracticeStep.planned(group, repetitionIndex, context));
                }
                group++;
            }
            boolean isLastRepetition = repetitionIndex == repetitions - 1;
            if (!isLastRepetition && restBetweenRepetitionsInSecond > 0) {
                steps.add(PracticeStep.planned(group, repetitionIndex, new CoolDownContext(restBetweenRepetitionsInSecond)));
                group++;
            }
        }
        return PracticeSession.create(exerciseId, steps);
    }

    /** Rests are added by this factory between repetitions; an activity must never produce one itself. */
    private static void assertNoRestInActivities(List<StepGroupBlueprint> pass) {
        boolean generatedARest = pass.stream()
                .flatMap(blueprint -> blueprint.steps().stream())
                .anyMatch(StepContext::isRest);
        if (generatedARest) {
            throw new IllegalStateException(
                    "An activity generated a rest step, but rests are only added between repetitions");
        }
    }
}

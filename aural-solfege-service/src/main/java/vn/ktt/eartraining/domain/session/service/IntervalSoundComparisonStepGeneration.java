package vn.ktt.eartraining.domain.session.service;

import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.exercise.valueobject.activity.IntervalSoundComparison;
import vn.ktt.eartraining.domain.session.valueobject.StepDefinition;
import vn.ktt.eartraining.domain.session.valueobject.StepType;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.IntervalSoundComparisonContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IntervalSoundComparisonStepGeneration implements IStepGeneration {
    private static final int TOTAL_QUESTIONS = 10;
    private final Random random = new Random();

    @Override
    public List<StepDefinition> generate(ExerciseActivity activity) {
        var comparisonActivity = (IntervalSoundComparison) activity;
        var intervals = comparisonActivity.getIntervals();

        var definitions = new ArrayList<StepDefinition>();
        for (int i = 1; i <= TOTAL_QUESTIONS; i++) {
            var firstInterval = intervals.get(random.nextInt(intervals.size()));
            var secondInterval = intervals.get(random.nextInt(intervals.size()));
            definitions.add(new StepDefinition(
                    comparisonActivity.getPosition(),
                    StepType.INTERVAL_SOUND_COMPARISON,
                    new IntervalSoundComparisonContext(
                            firstInterval,
                            secondInterval,
                            comparisonActivity.getTexture(),
                            TOTAL_QUESTIONS,
                            i)));
        }
        return definitions;
    }

    @Override
    public Class<? extends ExerciseActivity> getKey() {
        return IntervalSoundComparison.class;
    }

    @Override
    public IStepGeneration getService() {
        return this;
    }
}

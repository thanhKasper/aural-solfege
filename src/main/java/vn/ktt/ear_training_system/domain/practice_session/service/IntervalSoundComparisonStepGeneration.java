package vn.ktt.ear_training_system.domain.practice_session.service;

import vn.ktt.ear_training_system.domain.exercise.value_object.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.value_object.exercise_activity.IntervalSoundComparison;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepDefinition;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepType;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.IntervalSoundComparisonContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IntervalSoundComparisonStepGeneration implements StepGeneration {
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
    public StepGeneration getService() {
        return this;
    }
}

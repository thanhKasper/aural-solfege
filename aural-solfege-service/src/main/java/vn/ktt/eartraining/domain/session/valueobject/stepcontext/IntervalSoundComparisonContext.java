package vn.ktt.eartraining.domain.session.valueobject.stepcontext;

import vn.ktt.eartraining.domain.exercise.valueobject.IntervalTexture;
import vn.ktt.eartraining.domain.exercise.valueobject.MusicalInterval;
import vn.ktt.eartraining.domain.session.valueobject.StepType;

public record IntervalSoundComparisonContext(MusicalInterval firstInterval, MusicalInterval secondInterval,
                                             IntervalTexture texture, int totalQuestions,
                                             int currentQuestionNumber) implements StepContext {

    @Override
    public StepType getStepType() {
        return StepType.INTERVAL_SOUND_COMPARISON;
    }
}

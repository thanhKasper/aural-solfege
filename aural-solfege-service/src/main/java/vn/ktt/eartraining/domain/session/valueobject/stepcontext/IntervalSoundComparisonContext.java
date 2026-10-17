package vn.ktt.eartraining.domain.session.valueobject.stepcontext;

import vn.ktt.eartraining.domain.exercise.valueobject.IntervalTexture;
import vn.ktt.eartraining.domain.exercise.valueobject.MusicalInterval;

public record IntervalSoundComparisonContext(MusicalInterval firstInterval, MusicalInterval secondInterval,
                                             IntervalTexture texture, int totalQuestions,
                                             int currentQuestionNumber) implements StepContext {
}

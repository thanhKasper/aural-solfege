package vn.ktt.eartraining.infrastructure.persistence.entity.stepcontext;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.ktt.eartraining.domain.exercise.valueobject.IntervalTexture;
import vn.ktt.eartraining.domain.exercise.valueobject.MusicalInterval;

@Getter
@Setter
@NoArgsConstructor
public class IntervalSoundComparisonContextEntity extends StepContextEntity {
    private MusicalInterval firstInterval;
    private MusicalInterval secondInterval;
    private IntervalTexture texture;
    private int totalQuestions;
    private int currentQuestionNumber;

    public IntervalSoundComparisonContextEntity(MusicalInterval firstInterval, MusicalInterval secondInterval,
                                                IntervalTexture texture, int totalQuestions, int currentQuestionNumber) {
        this.firstInterval = firstInterval;
        this.secondInterval = secondInterval;
        this.texture = texture;
        this.totalQuestions = totalQuestions;
        this.currentQuestionNumber = currentQuestionNumber;
    }
}

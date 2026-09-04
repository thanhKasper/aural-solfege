package vn.ktt.ear_training_system.infrastructure.repository.entities.step_context;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;

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

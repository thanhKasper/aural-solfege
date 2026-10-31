package vn.ktt.eartraining.application.dto.step;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public final class IntervalSoundComparisonStepDTO implements PracticeStepDTO {
    private String firstInterval;
    private String secondInterval;
    private String texture;
    private int calculatedComparison; // The comparison between the first interval and the second interval.
    private int totalQuestions;
    private int currentQuestionNumber;

    public IntervalSoundComparisonStepDTO(String firstInterval, String secondInterval, String texture,
                                          int calculatedComparison, int totalQuestions, int currentQuestionNumber) {
        this.firstInterval = firstInterval;
        this.secondInterval = secondInterval;
        this.texture = texture;
        this.calculatedComparison = calculatedComparison;
        this.totalQuestions = totalQuestions;
        this.currentQuestionNumber = currentQuestionNumber;
    }
}

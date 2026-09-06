package vn.ktt.ear_training_system.application.dtos.practice_step;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public final class IntervalDistanceComparisonStepDTO implements PracticeStepDTO {
    private int activityPosition;
    private String status;
    private String firstInterval;
    private String secondInterval;
    private String texture;
    private int calculatedComparison; // The comparison between the first interval and the second interval.
    private int totalQuestions;
    private int currentQuestionNumber;

    public IntervalDistanceComparisonStepDTO(int activityPosition, String status, String firstInterval,
                                             String secondInterval, String texture, int calculatedComparison, int totalQuestions, int currentQuestionNumber) {
        this.activityPosition = activityPosition;
        this.status = status;
        this.firstInterval = firstInterval;
        this.secondInterval = secondInterval;
        this.texture = texture;
        this.calculatedComparison = calculatedComparison;
        this.totalQuestions = totalQuestions;
        this.currentQuestionNumber = currentQuestionNumber;
    }

    @Override
    public int activityPosition() { return activityPosition; }

    @Override
    public String status() { return status; }
}

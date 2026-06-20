package vn.ktt.ear_training_system.application.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public final class ListenIntervalStepDTO implements PracticeStepDTO {
    private int stepNumber;
    private int activityPosition;
    private PracticeStepType stepType;
    private String status;
    private String interval;
    private String direction;
    private String texture;

    public ListenIntervalStepDTO(int stepNumber, int activityPosition, PracticeStepType stepType, String status,
                                 String interval, String direction, String texture) {
        this.stepNumber = stepNumber;
        this.activityPosition = activityPosition;
        this.stepType = stepType;
        this.status = status;
        this.interval = interval;
        this.direction = direction;
        this.texture = texture;
    }

    @Override
    public int stepNumber() { return stepNumber; }

    @Override
    public int activityPosition() { return activityPosition; }

    @Override
    public PracticeStepType stepType() { return stepType; }

    @Override
    public String status() { return status; }
}

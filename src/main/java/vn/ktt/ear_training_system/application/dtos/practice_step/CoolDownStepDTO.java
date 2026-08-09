package vn.ktt.ear_training_system.application.dtos.practice_step;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public final class CoolDownStepDTO implements PracticeStepDTO {
    private int activityPosition;
    private String status;
    private int restingTimeInSecond;

    public CoolDownStepDTO(int activityPosition, String status, int restingTimeInSecond) {
        this.activityPosition = activityPosition;
        this.status = status;
        this.restingTimeInSecond = restingTimeInSecond;
    }

    @Override
    public int activityPosition() { return activityPosition; }

    @Override
    public String status() { return status; }
}

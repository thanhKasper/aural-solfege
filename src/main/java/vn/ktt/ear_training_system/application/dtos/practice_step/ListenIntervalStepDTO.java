package vn.ktt.ear_training_system.application.dtos.practice_step;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public final class ListenIntervalStepDTO implements PracticeStepDTO {
    private int activityPosition;
    private String status;
    private String interval;
    private String direction;
    private String texture;

    public ListenIntervalStepDTO(int activityPosition, String status,
                                 String interval, String direction, String texture) {
        this.activityPosition = activityPosition;
        this.status = status;
        this.interval = interval;
        this.direction = direction;
        this.texture = texture;
    }

    @Override
    public int activityPosition() { return activityPosition; }

    @Override
    public String status() { return status; }
}

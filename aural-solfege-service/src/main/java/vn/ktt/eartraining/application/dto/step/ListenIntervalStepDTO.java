package vn.ktt.eartraining.application.dto.step;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public final class ListenIntervalStepDTO implements PracticeStepDTO {
    private String interval;
    private String direction;
    private String texture;

    public ListenIntervalStepDTO(String interval, String direction, String texture) {
        this.interval = interval;
        this.direction = direction;
        this.texture = texture;
    }
}

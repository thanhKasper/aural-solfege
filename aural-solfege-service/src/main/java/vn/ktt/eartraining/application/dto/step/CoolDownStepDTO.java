package vn.ktt.eartraining.application.dto.step;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public final class CoolDownStepDTO implements PracticeStepDTO {
    private int restingTimeInSecond;

    public CoolDownStepDTO(int restingTimeInSecond) {
        this.restingTimeInSecond = restingTimeInSecond;
    }
}

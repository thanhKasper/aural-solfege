package vn.ktt.eartraining.infrastructure.persistence.entity.stepcontext;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CoolDownContextEntity extends StepContextEntity {
    private int restingTimeInSecond;

    public CoolDownContextEntity(int restingTimeInSecond) {
        this.restingTimeInSecond = restingTimeInSecond;
    }
}

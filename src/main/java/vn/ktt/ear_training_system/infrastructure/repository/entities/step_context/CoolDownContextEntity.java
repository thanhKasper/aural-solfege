package vn.ktt.ear_training_system.infrastructure.repository.entities.step_context;

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

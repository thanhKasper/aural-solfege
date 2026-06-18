package vn.ktt.ear_training_system.domain.practice_session.value_object;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class StepDefinition {
    private final int activityPosition;
    private final StepType stepType;
    private final StepContext context;

    public StepDefinition(int activityPosition, StepType stepType, StepContext context) {
        this.activityPosition = activityPosition;
        this.stepType = stepType;
        this.context = context;
    }
}

package vn.ktt.eartraining.domain.session.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.StepContext;

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

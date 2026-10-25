package vn.ktt.eartraining.domain.session.valueobject;

import vn.ktt.eartraining.domain.session.valueobject.stepcontext.StepContext;

import java.util.List;
import java.util.Objects;

/**
 * The steps generated for one activity of an exercise. A pass is an ordered list of these;
 * a session plays the pass as many times as it repeats.
 */
public record StepGroupBlueprint(List<StepContext> steps) {

    public StepGroupBlueprint {
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("A step group must contain at least one step");
        }
        if (steps.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("A step group cannot contain a null step");
        }
        steps = List.copyOf(steps);
    }
}

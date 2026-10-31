package vn.ktt.eartraining.domain.session.valueobject;

import org.junit.jupiter.api.Test;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.CoolDownContext;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.StepContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StepGroupBlueprintTest {

    @Test
    void rejectsNullAndEmptySteps() {
        assertThrows(IllegalArgumentException.class, () -> new StepGroupBlueprint(null));
        assertThrows(IllegalArgumentException.class, () -> new StepGroupBlueprint(List.of()));
    }

    @Test
    void rejectsANullStep() {
        List<StepContext> steps = Arrays.asList(new CoolDownContext(1), null);
        assertThrows(IllegalArgumentException.class, () -> new StepGroupBlueprint(steps));
    }

    @Test
    void copiesTheStepsDefensively() {
        var steps = new ArrayList<StepContext>(List.of(new CoolDownContext(1)));
        var blueprint = new StepGroupBlueprint(steps);

        steps.add(new CoolDownContext(2));

        assertEquals(1, blueprint.steps().size());
        assertThrows(UnsupportedOperationException.class, () -> blueprint.steps().add(new CoolDownContext(3)));
    }
}

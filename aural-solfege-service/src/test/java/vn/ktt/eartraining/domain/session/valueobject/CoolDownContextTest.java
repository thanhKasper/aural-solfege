package vn.ktt.eartraining.domain.session.valueobject;

import org.junit.jupiter.api.Test;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.CoolDownContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CoolDownContextTest {

    @Test
    void rejectsARestWithoutTime() {
        assertThrows(IllegalArgumentException.class, () -> new CoolDownContext(0));
        assertThrows(IllegalArgumentException.class, () -> new CoolDownContext(-5));
    }

    @Test
    void stepTypeIsDerivedFromTheContext() {
        assertEquals(StepType.COOL_DOWN, new CoolDownContext(5).getStepType());
    }
}

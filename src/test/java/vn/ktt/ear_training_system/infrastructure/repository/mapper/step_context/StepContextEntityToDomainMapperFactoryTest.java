package vn.ktt.ear_training_system.infrastructure.repository.mapper.step_context;

import org.junit.jupiter.api.Test;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.CoolDownContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.IntervalSoundComparisonContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.ListenIntervalContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.StepContext;
import vn.ktt.ear_training_system.infrastructure.repository.entities.step_context.CoolDownContextEntity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.step_context.IntervalSoundComparisonContextEntity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.step_context.ListenIntervalContextEntity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.step_context.StepContextEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StepContextEntityToDomainMapperFactoryTest {

    private final IStepContextEntityToDomainMapper coolDownMapper = new CoolDownContextEntityToDomainMapper();
    private final IStepContextEntityToDomainMapper listenMapper = new ListenIntervalContextEntityToDomainMapper();
    private final IStepContextEntityToDomainMapper comparisonMapper = new IntervalSoundComparisonContextEntityToDomainMapper();

    private final StepContextEntityToDomainMapperFactory factory =
            new StepContextEntityToDomainMapperFactory(List.of(coolDownMapper, listenMapper, comparisonMapper));

    @Test
    void transformCoolDownContextToEntity() {
        var context = new CoolDownContext(30);

        StepContextEntity entity = factory.toStepContextEntity(context);

        assertInstanceOf(CoolDownContextEntity.class, entity);
        assertEquals(30, ((CoolDownContextEntity) entity).getRestingTimeInSecond());
    }

    @Test
    void transformListenIntervalContextToEntity() {
        var context = new ListenIntervalContext(MusicalInterval.MAJOR_3RD, "ASC", IntervalTexture.ASCENDING);

        StepContextEntity entity = factory.toStepContextEntity(context);

        assertInstanceOf(ListenIntervalContextEntity.class, entity);
        var listenEntity = (ListenIntervalContextEntity) entity;
        assertEquals(MusicalInterval.MAJOR_3RD, listenEntity.getInterval());
        assertEquals("ASC", listenEntity.getDirection());
        assertEquals(IntervalTexture.ASCENDING, listenEntity.getTexture());
    }

    @Test
    void transformIntervalSoundComparisonContextToEntity() {
        var context = new IntervalSoundComparisonContext(
                MusicalInterval.PERFECT_5TH, MusicalInterval.MINOR_3RD, IntervalTexture.STACKED, 25, 4);

        StepContextEntity entity = factory.toStepContextEntity(context);

        assertInstanceOf(IntervalSoundComparisonContextEntity.class, entity);
        var comparisonEntity = (IntervalSoundComparisonContextEntity) entity;
        assertEquals(MusicalInterval.PERFECT_5TH, comparisonEntity.getFirstInterval());
        assertEquals(MusicalInterval.MINOR_3RD, comparisonEntity.getSecondInterval());
        assertEquals(IntervalTexture.STACKED, comparisonEntity.getTexture());
        assertEquals(25, comparisonEntity.getTotalQuestions());
        assertEquals(4, comparisonEntity.getCurrentQuestionNumber());
    }

    @Test
    void transformWithExplicitTargetClassDisambiguates() {
        var context = new ListenIntervalContext(MusicalInterval.PERFECT_4TH, "DESC", IntervalTexture.DESCENDING);

        StepContextEntity entity = factory.toStepContextEntity(context, ListenIntervalContextEntity.class);

        assertInstanceOf(ListenIntervalContextEntity.class, entity);
    }

    @Test
    void reverseTransformCoolDownEntityToDomain() {
        var entity = new CoolDownContextEntity(15);

        StepContext context = factory.toStepContext(entity);

        assertEquals(new CoolDownContext(15), context);
    }

    @Test
    void reverseTransformListenIntervalEntityToDomain() {
        var entity = new ListenIntervalContextEntity(MusicalInterval.MINOR_6TH, "DESC", IntervalTexture.ASCENDING);

        StepContext context = factory.toStepContext(entity);

        assertEquals(new ListenIntervalContext(MusicalInterval.MINOR_6TH, "DESC", IntervalTexture.ASCENDING), context);
    }

    @Test
    void reverseTransformIntervalSoundComparisonEntityToDomain() {
        var entity = new IntervalSoundComparisonContextEntity(
                MusicalInterval.TRITONE, MusicalInterval.MAJOR_2ND, IntervalTexture.STACKED, 10, 7);

        StepContext context = factory.toStepContext(entity);

        assertEquals(new IntervalSoundComparisonContext(
                MusicalInterval.TRITONE, MusicalInterval.MAJOR_2ND, IntervalTexture.STACKED, 10, 7), context);
    }

    @Test
    void contextToEntityRoundTripPreservesDomainValues() {
        var original = new IntervalSoundComparisonContext(
                MusicalInterval.MAJOR_2ND, MusicalInterval.PERFECT_4TH, IntervalTexture.DESCENDING, 8, 3);

        StepContext recreated = factory.toStepContext(factory.toStepContextEntity(original));

        assertEquals(original, recreated);
    }

    @Test
    void transformWithMismatchedTargetThrows() {
        var context = new CoolDownContext(5);

        assertThrows(IllegalArgumentException.class,
                () -> factory.toStepContextEntity(context, ListenIntervalContextEntity.class));
    }

    @Test
    void reverseTransformWithMismatchedSourceThrows() {
        var entity = new CoolDownContextEntity(5);

        assertThrows(IllegalArgumentException.class,
                () -> factory.toStepContext(entity, ListenIntervalContext.class));
    }
}
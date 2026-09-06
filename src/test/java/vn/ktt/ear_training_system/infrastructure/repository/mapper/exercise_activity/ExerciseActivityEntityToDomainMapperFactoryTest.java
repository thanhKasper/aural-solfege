package vn.ktt.ear_training_system.infrastructure.repository.mapper.exercise_activity;

import org.junit.jupiter.api.Test;
import vn.ktt.ear_training_system.domain.exercise.value_object.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;
import vn.ktt.ear_training_system.domain.exercise.value_object.exercise_activity.CoolDownRestActivity;
import vn.ktt.ear_training_system.domain.exercise.value_object.exercise_activity.IntervalSoundComparison;
import vn.ktt.ear_training_system.domain.exercise.value_object.exercise_activity.SingleIntervalExerciseActivity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities.CoolDownRestActivityEntity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities.ExerciseActivityEntity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities.IntervalSoundComparisonExerciseActivityEntity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities.SingleIntervalExerciseActivityEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExerciseActivityEntityToDomainMapperFactoryTest {

    private final IExerciseActivityEntityToDomainMapper singleMapper = new SingleIntervalEntityToDomainMapper();
    private final IExerciseActivityEntityToDomainMapper comparisonMapper = new IntervalSoundComparisonEntityToDomainMapper();
    private final IExerciseActivityEntityToDomainMapper coolDownMapper = new CoolDownActivityEntityToDomainMapper();

    private final ExerciseActivityEntityToDomainMapperFactory factory =
            new ExerciseActivityEntityToDomainMapperFactory(List.of(singleMapper, comparisonMapper, coolDownMapper));

    @Test
    void transformSingleIntervalActivityToEntity() {
        var activity = new SingleIntervalExerciseActivity(
                IntervalTexture.ASCENDING, List.of(MusicalInterval.MAJOR_3RD), 1);

        ExerciseActivityEntity entity = factory.toExerciseActivityEntity(activity);

        assertInstanceOf(SingleIntervalExerciseActivityEntity.class, entity);
        var singleEntity = (SingleIntervalExerciseActivityEntity) entity;
        assertEquals(IntervalTexture.ASCENDING, singleEntity.getSoundProperty());
        assertEquals(List.of(MusicalInterval.MAJOR_3RD), singleEntity.getIntervals());
        assertEquals(1, singleEntity.getPosition());
    }

    @Test
    void transformIntervalSoundComparisonToEntity() {
        var activity = IntervalSoundComparison.construct(
                2, IntervalTexture.STACKED, MusicalInterval.PERFECT_5TH, MusicalInterval.MINOR_3RD);

        ExerciseActivityEntity entity = factory.toExerciseActivityEntity(activity);

        assertInstanceOf(IntervalSoundComparisonExerciseActivityEntity.class, entity);
        var comparisonEntity = (IntervalSoundComparisonExerciseActivityEntity) entity;
        assertEquals(IntervalTexture.STACKED, comparisonEntity.getTexture());
        assertEquals(MusicalInterval.PERFECT_5TH, comparisonEntity.getFirstInterval());
        assertEquals(MusicalInterval.MINOR_3RD, comparisonEntity.getSecondInterval());
        assertEquals(2, comparisonEntity.getPosition());
    }

    @Test
    void transformCoolDownActivityToEntity() {
        var activity = new CoolDownRestActivity(3, 30);

        ExerciseActivityEntity entity = factory.toExerciseActivityEntity(activity);

        assertInstanceOf(CoolDownRestActivityEntity.class, entity);
        var coolDownEntity = (CoolDownRestActivityEntity) entity;
        assertEquals(3, coolDownEntity.getPosition());
        assertEquals(30, coolDownEntity.getRestAmountInSecond());
    }

    @Test
    void transformWithExplicitTargetClassDisambiguates() {
        var activity = new SingleIntervalExerciseActivity(
                IntervalTexture.DESCENDING, List.of(MusicalInterval.PERFECT_4TH), 4);

        ExerciseActivityEntity entity = factory.toExerciseActivityEntity(activity, SingleIntervalExerciseActivityEntity.class);

        assertInstanceOf(SingleIntervalExerciseActivityEntity.class, entity);
    }

    @Test
    void reverseTransformSingleIntervalEntityToDomain() {
        var entity = new SingleIntervalExerciseActivityEntity(
                IntervalTexture.ASCENDING, List.of(MusicalInterval.MINOR_6TH), 5);

        ExerciseActivity domain = factory.toExerciseActivity(entity);

        assertInstanceOf(SingleIntervalExerciseActivity.class, domain);
        var single = (SingleIntervalExerciseActivity) domain;
        assertEquals(IntervalTexture.ASCENDING, single.getSoundProperty());
        assertEquals(List.of(MusicalInterval.MINOR_6TH), single.getIntervals());
        assertEquals(5, single.getPosition());
    }

    @Test
    void reverseTransformIntervalSoundComparisonEntityToDomain() {
        var entity = new IntervalSoundComparisonExerciseActivityEntity(
                IntervalTexture.STACKED, MusicalInterval.TRITONE, MusicalInterval.MAJOR_2ND, 6);

        ExerciseActivity domain = factory.toExerciseActivity(entity);

        assertInstanceOf(IntervalSoundComparison.class, domain);
        var comparison = (IntervalSoundComparison) domain;
        assertEquals(MusicalInterval.TRITONE, comparison.getFirstInterval());
        assertEquals(MusicalInterval.MAJOR_2ND, comparison.getSecondInterval());
        assertEquals(IntervalTexture.STACKED, comparison.getTexture());
        assertEquals(6, comparison.getPosition());
    }

    @Test
    void reverseTransformCoolDownEntityToDomain() {
        var entity = new CoolDownRestActivityEntity(7, 45);

        ExerciseActivity domain = factory.toExerciseActivity(entity);

        assertInstanceOf(CoolDownRestActivity.class, domain);
        var coolDown = (CoolDownRestActivity) domain;
        assertEquals(7, coolDown.getPosition());
        assertEquals(45, coolDown.getRestAmountInSecond());
    }

    @Test
    void activityToEntityRoundTripPreservesDomainValues() {
        var original = IntervalSoundComparison.construct(
                8, IntervalTexture.ASCENDING, MusicalInterval.MAJOR_2ND, MusicalInterval.PERFECT_4TH);

        ExerciseActivity recreated = factory.toExerciseActivity(factory.toExerciseActivityEntity(original));

        assertEquals(original.getPosition(), recreated.getPosition());
        assertEquals(original.getIntervals(), recreated.getIntervals());
    }

    @Test
    void transformWithMismatchedTargetThrows() {
        var activity = new CoolDownRestActivity(1, 10);

        assertThrows(IllegalArgumentException.class,
                () -> factory.toExerciseActivityEntity(activity, SingleIntervalExerciseActivityEntity.class));
    }

    @Test
    void reverseTransformWithMismatchedSourceThrows() {
        var entity = new CoolDownRestActivityEntity(1, 10);

        assertThrows(IllegalArgumentException.class,
                () -> factory.toExerciseActivity(entity, SingleIntervalExerciseActivity.class));
    }
}
package vn.ktt.ear_training_system.application.mappers.exercise_activity;

import org.junit.jupiter.api.Test;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.CoolDownRestActivityDTO;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.ExerciseActivityDTO;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.IntervalSoundComparisonExerciseActivityDTO;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.SingleIntervalExerciseActivityDTO;
import vn.ktt.ear_training_system.domain.exercise.value_object.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;
import vn.ktt.ear_training_system.domain.exercise.value_object.exercise_activity.CoolDownRestActivity;
import vn.ktt.ear_training_system.domain.exercise.value_object.exercise_activity.IntervalSoundComparison;
import vn.ktt.ear_training_system.domain.exercise.value_object.exercise_activity.SingleIntervalExerciseActivity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExerciseActivityDTOToDomainMapperFactoryTest {

    private final IExerciseActivityDTOToDomainMapper singleMapper = new SingleIntervalExerciseActivityDTOtoDomainMapper();
    private final IExerciseActivityDTOToDomainMapper comparisonMapper = new IntervalSoundComparisonExerciseActivityDTOtoDomainMapper();
    private final IExerciseActivityDTOToDomainMapper coolDownMapper = new CoolDownActivityDTOToDomainMapper();

    private final ExerciseActivityDTOToDomainMapperFactory factory =
            new ExerciseActivityDTOToDomainMapperFactory(List.of(singleMapper, comparisonMapper, coolDownMapper));

    @Test
    void transformSingleIntervalActivityToDto() {
        var activity = new SingleIntervalExerciseActivity(
                IntervalTexture.ASCENDING, List.of(MusicalInterval.MAJOR_3RD), 1);

        ExerciseActivityDTO dto = factory.toExerciseActivityDTO(activity);

        assertInstanceOf(SingleIntervalExerciseActivityDTO.class, dto);
        var singleDto = (SingleIntervalExerciseActivityDTO) dto;
        assertEquals(List.of("MAJOR_3RD"), singleDto.getIntervals());
        assertEquals("ASCENDING", singleDto.getTexture());
        assertEquals(1, singleDto.getPosition());
    }

    @Test
    void transformIntervalSoundComparisonToDto() {
        var activity = IntervalSoundComparison.construct(
                2, IntervalTexture.STACKED, MusicalInterval.PERFECT_5TH, MusicalInterval.MINOR_3RD);

        ExerciseActivityDTO dto = factory.toExerciseActivityDTO(activity);

        assertInstanceOf(IntervalSoundComparisonExerciseActivityDTO.class, dto);
        var comparisonDto = (IntervalSoundComparisonExerciseActivityDTO) dto;
        assertEquals(List.of("PERFECT_5TH", "MINOR_3RD"), comparisonDto.getIntervals());
        assertEquals("STACKED", comparisonDto.getTexture());
        assertEquals(2, comparisonDto.getPosition());
    }

    @Test
    void transformCoolDownActivityToDto() {
        var activity = new CoolDownRestActivity(3, 30);

        ExerciseActivityDTO dto = factory.toExerciseActivityDTO(activity);

        assertInstanceOf(CoolDownRestActivityDTO.class, dto);
        var coolDownDto = (CoolDownRestActivityDTO) dto;
        assertEquals(3, coolDownDto.getPosition());
        assertEquals(30, coolDownDto.getRestTime());
    }

    @Test
    void transformWithExplicitTargetClassDisambiguates() {
        var activity = new SingleIntervalExerciseActivity(
                IntervalTexture.DESCENDING, List.of(MusicalInterval.PERFECT_4TH), 4);

        ExerciseActivityDTO dto = factory.toExerciseActivityDTO(activity, SingleIntervalExerciseActivityDTO.class);

        assertInstanceOf(SingleIntervalExerciseActivityDTO.class, dto);
    }

    @Test
    void reverseTransformSingleIntervalDtoToDomain() {
        var dto = new SingleIntervalExerciseActivityDTO(List.of("MINOR_6TH"), "ASCENDING", 5);

        ExerciseActivity domain = factory.toExerciseActivityDomain(dto);

        assertInstanceOf(SingleIntervalExerciseActivity.class, domain);
        var single = (SingleIntervalExerciseActivity) domain;
        assertEquals(List.of(MusicalInterval.MINOR_6TH), single.getIntervals());
        assertEquals(IntervalTexture.ASCENDING, single.getSoundProperty());
        assertEquals(5, single.getPosition());
    }

    @Test
    void reverseTransformIntervalSoundComparisonDtoToDomain() {
        var dto = new IntervalSoundComparisonExerciseActivityDTO(List.of("TRITONE", "MAJOR_2ND"), "STACKED", 6);

        ExerciseActivity domain = factory.toExerciseActivityDomain(dto);

        assertInstanceOf(IntervalSoundComparison.class, domain);
        var comparison = (IntervalSoundComparison) domain;
        assertEquals(MusicalInterval.TRITONE, comparison.getFirstInterval());
        assertEquals(MusicalInterval.MAJOR_2ND, comparison.getSecondInterval());
        assertEquals(IntervalTexture.STACKED, comparison.getTexture());
        assertEquals(6, comparison.getPosition());
    }

    @Test
    void reverseTransformCoolDownDtoToDomain() {
        var dto = new CoolDownRestActivityDTO(7, 45);

        ExerciseActivity domain = factory.toExerciseActivityDomain(dto);

        assertInstanceOf(CoolDownRestActivity.class, domain);
        var coolDown = (CoolDownRestActivity) domain;
        assertEquals(7, coolDown.getPosition());
        assertEquals(45, coolDown.getRestAmountInSecond());
    }

    @Test
    void activityToDtoRoundTripPreservesDomainValues() {
        var original = IntervalSoundComparison.construct(
                8, IntervalTexture.ASCENDING, MusicalInterval.MAJOR_2ND, MusicalInterval.PERFECT_4TH);

        ExerciseActivity recreated = factory.toExerciseActivityDomain(factory.toExerciseActivityDTO(original));

        assertEquals(original.getPosition(), recreated.getPosition());
        assertEquals(original.getIntervals(), recreated.getIntervals());
    }

    @Test
    void transformWithMismatchedTargetThrows() {
        var activity = new CoolDownRestActivity(1, 10);

        assertThrows(IllegalArgumentException.class,
                () -> factory.toExerciseActivityDTO(activity, SingleIntervalExerciseActivityDTO.class));
    }

    @Test
    void reverseTransformWithMismatchedSourceThrows() {
        var dto = new CoolDownRestActivityDTO(1, 10);

        assertThrows(IllegalArgumentException.class,
                () -> factory.toExerciseActivityDomain(dto, SingleIntervalExerciseActivity.class));
    }
}
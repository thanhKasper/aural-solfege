package vn.ktt.eartraining.application.mapper.activity;

import org.junit.jupiter.api.Test;
import vn.ktt.eartraining.application.dto.activity.ExerciseActivityDTO;
import vn.ktt.eartraining.application.dto.activity.IntervalSoundComparisonExerciseActivityDTO;
import vn.ktt.eartraining.application.dto.activity.SingleIntervalExerciseActivityDTO;
import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.exercise.valueobject.IntervalTexture;
import vn.ktt.eartraining.domain.exercise.valueobject.MusicalInterval;
import vn.ktt.eartraining.domain.exercise.valueobject.activity.IntervalSoundComparison;
import vn.ktt.eartraining.domain.exercise.valueobject.activity.SingleIntervalExerciseActivity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExerciseActivityDTOToDomainMapperFactoryTest {

    private final IExerciseActivityDTOToDomainMapper singleMapper = new SingleIntervalExerciseActivityDTOtoDomainMapper();
    private final IExerciseActivityDTOToDomainMapper comparisonMapper = new IntervalSoundComparisonExerciseActivityDTOtoDomainMapper();

    private final ExerciseActivityDTOToDomainMapperFactory factory =
            new ExerciseActivityDTOToDomainMapperFactory(List.of(singleMapper, comparisonMapper));

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
    void activityToDtoRoundTripPreservesDomainValues() {
        var original = IntervalSoundComparison.construct(
                8, IntervalTexture.ASCENDING, MusicalInterval.MAJOR_2ND, MusicalInterval.PERFECT_4TH);

        ExerciseActivity recreated = factory.toExerciseActivityDomain(factory.toExerciseActivityDTO(original));

        assertEquals(original.getPosition(), recreated.getPosition());
        assertEquals(original.getIntervals(), recreated.getIntervals());
    }

    @Test
    void transformWithMismatchedTargetThrows() {
        var activity = IntervalSoundComparison.construct(
                1, IntervalTexture.ASCENDING, MusicalInterval.MAJOR_2ND, MusicalInterval.PERFECT_4TH);

        assertThrows(IllegalArgumentException.class,
                () -> factory.toExerciseActivityDTO(activity, SingleIntervalExerciseActivityDTO.class));
    }

    @Test
    void reverseTransformWithMismatchedSourceThrows() {
        var dto = new IntervalSoundComparisonExerciseActivityDTO(List.of("MAJOR_2ND", "PERFECT_4TH"), "ASCENDING", 1);

        assertThrows(IllegalArgumentException.class,
                () -> factory.toExerciseActivityDomain(dto, SingleIntervalExerciseActivity.class));
    }
}
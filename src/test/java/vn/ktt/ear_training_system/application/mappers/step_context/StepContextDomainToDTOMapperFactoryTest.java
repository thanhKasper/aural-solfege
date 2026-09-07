package vn.ktt.ear_training_system.application.mappers.step_context;

import org.junit.jupiter.api.Test;
import vn.ktt.ear_training_system.application.dtos.practice_step.CoolDownStepDTO;
import vn.ktt.ear_training_system.application.dtos.practice_step.IntervalDistanceComparisonStepDTO;
import vn.ktt.ear_training_system.application.dtos.practice_step.ListenIntervalStepDTO;
import vn.ktt.ear_training_system.application.dtos.practice_step.PracticeStepDTO;
import vn.ktt.ear_training_system.application.outbound.IIntervalComparisonPort;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.CoolDownContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.IntervalSoundComparisonContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.ListenIntervalContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.StepContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StepContextDomainToDTOMapperFactoryTest {

    private final IIntervalComparisonPort sampleComparisonPort =
            (firstInterval, secondInterval) -> Integer.compare(firstInterval.ordinal(), secondInterval.ordinal());

    private final IStepContextMapper coolDownMapper = new CoolDownStepContextMapper();
    private final IStepContextMapper listenMapper = new ListenIntervalStepContextMapper();
    private final IStepContextMapper comparisonMapper = new IntervalDistanceComparisonStepContextMapper(sampleComparisonPort);

    private final StepContextDomainToDTOMapperFactory factory =
            new StepContextDomainToDTOMapperFactory(List.of(coolDownMapper, listenMapper, comparisonMapper));

    @Test
    void transformCoolDownContextToDto() {
        var context = new CoolDownContext(30);

        PracticeStepDTO dto = factory.toDto(context);

        assertInstanceOf(CoolDownStepDTO.class, dto);
        var coolDownDto = (CoolDownStepDTO) dto;
        assertEquals(30, coolDownDto.getRestingTimeInSecond());
        assertEquals(0, dto.activityPosition());
        assertEquals("", dto.status());
    }

    @Test
    void transformListenIntervalContextToDto() {
        var context = new ListenIntervalContext(MusicalInterval.MAJOR_3RD, "ASC", IntervalTexture.ASCENDING);

        PracticeStepDTO dto = factory.toDto(context);

        assertInstanceOf(ListenIntervalStepDTO.class, dto);
        var listenDto = (ListenIntervalStepDTO) dto;
        assertEquals("MAJOR_3RD", listenDto.getInterval());
        assertEquals("ASC", listenDto.getDirection());
        assertEquals("ASCENDING", listenDto.getTexture());
    }

    @Test
    void transformIntervalSoundComparisonContextToDto() {
        var context = new IntervalSoundComparisonContext(
                MusicalInterval.PERFECT_5TH, MusicalInterval.MINOR_3RD, IntervalTexture.STACKED, 25, 4);

        PracticeStepDTO dto = factory.toDto(context);

        assertInstanceOf(IntervalDistanceComparisonStepDTO.class, dto);
        var comparisonDto = (IntervalDistanceComparisonStepDTO) dto;
        assertEquals("PERFECT_5TH", comparisonDto.getFirstInterval());
        assertEquals("MINOR_3RD", comparisonDto.getSecondInterval());
        assertEquals("STACKED", comparisonDto.getTexture());
        assertEquals(1, comparisonDto.getCalculatedComparison());
        assertEquals(25, comparisonDto.getTotalQuestions());
        assertEquals(4, comparisonDto.getCurrentQuestionNumber());
    }

    @Test
    void transformWithExplicitTargetClassDisambiguates() {
        var context = new ListenIntervalContext(MusicalInterval.PERFECT_4TH, "DESC", IntervalTexture.DESCENDING);

        PracticeStepDTO dto = factory.toDto(context, ListenIntervalStepDTO.class);

        assertInstanceOf(ListenIntervalStepDTO.class, dto);
    }

    @Test
    void reverseTransformCoolDownDtoToDomain() {
        var dto = new CoolDownStepDTO(3, "COMPLETED", 15);

        StepContext context = factory.toDomain(dto);

        assertEquals(new CoolDownContext(15), context);
    }

    @Test
    void reverseTransformListenIntervalDtoToDomain() {
        var dto = new ListenIntervalStepDTO(2, "PENDING", "MINOR_6TH", "DESC", "ASCENDING");

        StepContext context = factory.toDomain(dto);

        assertEquals(new ListenIntervalContext(MusicalInterval.MINOR_6TH, "DESC", IntervalTexture.ASCENDING), context);
    }

    @Test
    void reverseTransformIntervalSoundComparisonDtoToDomain() {
        var dto = new IntervalDistanceComparisonStepDTO(1, "PENDING", "TRITONE", "MAJOR_2ND", "STACKED", -3, 10, 7);

        StepContext context = factory.toDomain(dto);

        assertEquals(new IntervalSoundComparisonContext(
                MusicalInterval.TRITONE, MusicalInterval.MAJOR_2ND, IntervalTexture.STACKED, 10, 7), context);
    }

    @Test
    void reverseTransformWithExplicitSourceClassDisambiguates() {
        var dto = new ListenIntervalStepDTO(0, "PENDING", "UNISON", "ASC", "ASCENDING");

        StepContext context = factory.toDomain(dto, ListenIntervalContext.class);

        assertEquals(new ListenIntervalContext(MusicalInterval.UNISON, "ASC", IntervalTexture.ASCENDING), context);
    }

    @Test
    void contextToDtoRoundTripPreservesDomainValues() {
        var original = new IntervalSoundComparisonContext(
                MusicalInterval.MAJOR_2ND, MusicalInterval.PERFECT_4TH, IntervalTexture.DESCENDING, 8, 3);

        StepContext recreated = factory.toDomain(factory.toDto(original));

        assertEquals(original, recreated);
    }

    @Test
    void transformWithMismatchedTargetThrows() {
        var context = new CoolDownContext(5);

        assertThrows(IllegalArgumentException.class,
                () -> factory.toDto(context, ListenIntervalStepDTO.class));
    }

    @Test
    void reverseTransformWithMismatchedSourceThrows() {
        var dto = new CoolDownStepDTO(0, "PENDING", 5);

        assertThrows(IllegalArgumentException.class,
                () -> factory.toDomain(dto, ListenIntervalContext.class));
    }
}
package vn.ktt.eartraining.infrastructure.config;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;
import vn.ktt.eartraining.application.dto.activity.CoolDownRestActivityDTO;
import vn.ktt.eartraining.application.dto.activity.ExerciseActivityDTO;
import vn.ktt.eartraining.application.dto.activity.IntervalSoundComparisonExerciseActivityDTO;
import vn.ktt.eartraining.application.dto.activity.SingleIntervalExerciseActivityDTO;
import vn.ktt.eartraining.application.dto.step.CoolDownStepDTO;
import vn.ktt.eartraining.application.dto.step.IntervalSoundComparisonStepDTO;
import vn.ktt.eartraining.application.dto.step.ListenIntervalStepDTO;
import vn.ktt.eartraining.application.dto.step.PracticeStepDTO;
import vn.ktt.eartraining.infrastructure.jackson.provider.activity.CoolDownActivityDTOProvider;
import vn.ktt.eartraining.infrastructure.jackson.provider.activity.IntervalSoundComparisonExerciseActivityDTOProvider;
import vn.ktt.eartraining.infrastructure.jackson.provider.activity.SingleIntervalExerciseActivityDTOProvider;
import vn.ktt.eartraining.infrastructure.jackson.provider.step.CoolDownStepDTOProvider;
import vn.ktt.eartraining.infrastructure.jackson.provider.step.IntervalSoundComparisonStepDTOProvider;
import vn.ktt.eartraining.infrastructure.jackson.provider.step.ListenIntervalStepDTOProvider;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Pins the wire format produced by {@link JacksonConfiguration}.
 *
 * <p>The polymorphic DTO wiring - mixins plus subtype registration - is what puts the
 * {@code "type"} discriminator into every REST response, and it is configured through
 * Spring's Jackson auto-configuration rather than through annotations on the DTOs. That
 * makes it invisible to the plain unit tests: they construct mappers themselves and would
 * stay green even if this configuration stopped being applied entirely. These tests go
 * through the container-supplied mapper so a break in that wiring fails the build.
 */
@JsonTest
@Import({
        JacksonConfiguration.class,
        CoolDownActivityDTOProvider.class,
        SingleIntervalExerciseActivityDTOProvider.class,
        IntervalSoundComparisonExerciseActivityDTOProvider.class,
        CoolDownStepDTOProvider.class,
        ListenIntervalStepDTOProvider.class,
        IntervalSoundComparisonStepDTOProvider.class
})
class PolymorphicDtoSerializationTest {

    @Autowired
    private ObjectMapper mapper;

    static Stream<Arguments> exerciseActivities() {
        return Stream.of(
                arguments("COOL_DOWN",
                        new CoolDownRestActivityDTO(2, 30)),
                arguments("SINGLE_INTERVAL",
                        new SingleIntervalExerciseActivityDTO(List.of("MAJOR_2ND"), "ASCENDING", 0)),
                arguments("INTERVAL_SOUND_COMPARISON",
                        new IntervalSoundComparisonExerciseActivityDTO(
                                List.of("PERFECT_4TH", "PERFECT_5TH"), "ASCENDING", 1))
        );
    }

    static Stream<Arguments> practiceSteps() {
        return Stream.of(
                arguments("COOL_DOWN",
                        new CoolDownStepDTO(2, "ACTIVE", 30)),
                arguments("LISTEN_INTERVAL",
                        new ListenIntervalStepDTO(0, "ACTIVE", "MAJOR_2ND", "UP", "ASCENDING")),
                arguments("INTERVAL_SOUND_COMPARISON",
                        new IntervalSoundComparisonStepDTO(
                                1, "PENDING", "PERFECT_4TH", "PERFECT_5TH", "ASCENDING", -1, 10, 3))
        );
    }

    @ParameterizedTest(name = "exercise activity {0} carries its type discriminator")
    @MethodSource("exerciseActivities")
    void exerciseActivityIsWrittenWithItsTypeDiscriminator(String expectedType, ExerciseActivityDTO dto)
            throws Exception {
        String json = mapper.writerFor(ExerciseActivityDTO.class).writeValueAsString(dto);

        assertEquals(expectedType, mapper.readTree(json).path("type").asText(),
                () -> "missing or wrong type discriminator in " + json);
    }

    @ParameterizedTest(name = "exercise activity {0} round-trips to its own subtype")
    @MethodSource("exerciseActivities")
    void exerciseActivityRoundTripsToTheSameSubtype(String expectedType, ExerciseActivityDTO dto)
            throws Exception {
        String json = mapper.writerFor(ExerciseActivityDTO.class).writeValueAsString(dto);

        ExerciseActivityDTO read = mapper.readValue(json, ExerciseActivityDTO.class);

        assertInstanceOf(dto.getClass(), read);
        assertEquals(dto.position(), read.position());
        assertEquals(expectedType, read.type().name());
    }

    @ParameterizedTest(name = "practice step {0} carries its type discriminator")
    @MethodSource("practiceSteps")
    void practiceStepIsWrittenWithItsTypeDiscriminator(String expectedType, PracticeStepDTO dto)
            throws Exception {
        String json = mapper.writerFor(PracticeStepDTO.class).writeValueAsString(dto);

        assertEquals(expectedType, mapper.readTree(json).path("type").asText(),
                () -> "missing or wrong type discriminator in " + json);
    }

    @ParameterizedTest(name = "practice step {0} round-trips to its own subtype")
    @MethodSource("practiceSteps")
    void practiceStepRoundTripsToTheSameSubtype(String expectedType, PracticeStepDTO dto)
            throws Exception {
        String json = mapper.writerFor(PracticeStepDTO.class).writeValueAsString(dto);

        PracticeStepDTO read = mapper.readValue(json, PracticeStepDTO.class);

        assertInstanceOf(dto.getClass(), read);
        assertEquals(dto.activityPosition(), read.activityPosition());
        assertEquals(dto.status(), read.status());
    }

    /**
     * Activities reach the client nested inside {@code ExerciseDTO}, declared as
     * {@code List<ExerciseActivityDTO>}. The discriminator has to survive that too,
     * which is a different Jackson code path from writing a single value.
     */
    @org.junit.jupiter.api.Test
    void activitiesNestedInAListKeepTheirDiscriminators() throws Exception {
        List<ExerciseActivityDTO> activities = List.of(
                new SingleIntervalExerciseActivityDTO(List.of("MAJOR_2ND"), "ASCENDING", 0),
                new CoolDownRestActivityDTO(1, 100));

        String json = mapper.writerFor(new tools.jackson.core.type.TypeReference<List<ExerciseActivityDTO>>() {})
                .writeValueAsString(activities);
        var tree = mapper.readTree(json);

        assertEquals("SINGLE_INTERVAL", tree.get(0).path("type").asText());
        assertEquals("COOL_DOWN", tree.get(1).path("type").asText());
    }
}

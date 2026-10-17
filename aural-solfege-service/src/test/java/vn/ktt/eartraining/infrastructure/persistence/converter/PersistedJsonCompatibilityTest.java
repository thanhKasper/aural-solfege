package vn.ktt.eartraining.infrastructure.persistence.converter;

import org.junit.jupiter.api.Test;
import vn.ktt.eartraining.domain.exercise.valueobject.IntervalTexture;
import vn.ktt.eartraining.domain.exercise.valueobject.MusicalInterval;
import vn.ktt.eartraining.infrastructure.persistence.entity.activity.CoolDownRestActivityEntity;
import vn.ktt.eartraining.infrastructure.persistence.entity.activity.ExerciseActivityEntity;
import vn.ktt.eartraining.infrastructure.persistence.entity.activity.IntervalSoundComparisonExerciseActivityEntity;
import vn.ktt.eartraining.infrastructure.persistence.entity.activity.SingleIntervalExerciseActivityEntity;
import vn.ktt.eartraining.infrastructure.persistence.entity.stepcontext.CoolDownContextEntity;
import vn.ktt.eartraining.infrastructure.persistence.entity.stepcontext.ListenIntervalContextEntity;
import vn.ktt.eartraining.infrastructure.persistence.entity.stepcontext.StepContextEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Pins the JSON format stored in the {@code exercises.exercise_activities} and
 * {@code practice_steps.context} columns.
 *
 * <p>Both columns hold Jackson-serialized polymorphic documents written by
 * {@link ExerciseActivitiesConverter} and {@link StepContextEntityConverter}. Nothing else
 * in the test suite reads that format, so a change in Jackson behaviour - a different
 * discriminator, a renamed property, stricter unknown-field handling - would leave every
 * test green and only surface as unreadable rows at runtime.
 *
 * <p>The activity payloads below are copied verbatim from {@code src/main/resources/import.sql},
 * so this also guards the seed data itself.
 */
class PersistedJsonCompatibilityTest {

    private static final String BASIC_INTERVAL_TRAINING_ACTIVITIES = """
            [{"type": "SINGLE_INTERVAL", "soundProperty": "ASCENDING", "intervals": ["MAJOR_2ND"], "position": 0},\
             {"type": "SINGLE_INTERVAL", "soundProperty": "ASCENDING", "intervals": ["PERFECT_5TH"], "position": 1},\
             {"type": "COOL_DOWN", "position": 2, "restAmountInSecond": 100}]""";

    private static final String INTERVAL_COMPARISON_ACTIVITIES = """
            [{"type": "INTERVAL_SOUND_COMPARISON", "texture": "ASCENDING", "firstInterval": "PERFECT_4TH",\
             "secondInterval": "PERFECT_5TH", "position": 0},\
             {"type": "COOL_DOWN", "position": 1, "restAmountInSecond": 100}]""";

    private final ExerciseActivitiesConverter activitiesConverter = new ExerciseActivitiesConverter();
    private final StepContextEntityConverter contextConverter = new StepContextEntityConverter();

    @Test
    void seededSingleIntervalExerciseStillDeserializes() {
        List<ExerciseActivityEntity> activities =
                activitiesConverter.convertToEntityAttribute(BASIC_INTERVAL_TRAINING_ACTIVITIES);

        assertEquals(3, activities.size());

        var first = assertInstanceOf(SingleIntervalExerciseActivityEntity.class, activities.get(0));
        assertEquals(IntervalTexture.ASCENDING, first.getSoundProperty());
        assertEquals(List.of(MusicalInterval.MAJOR_2ND), first.getIntervals());
        assertEquals(0, first.getPosition());

        var cooldown = assertInstanceOf(CoolDownRestActivityEntity.class, activities.get(2));
        assertEquals(100, cooldown.getRestAmountInSecond());
        assertEquals(2, cooldown.getPosition());
    }

    @Test
    void seededComparisonExerciseStillDeserializes() {
        List<ExerciseActivityEntity> activities =
                activitiesConverter.convertToEntityAttribute(INTERVAL_COMPARISON_ACTIVITIES);

        assertEquals(2, activities.size());

        var comparison = assertInstanceOf(IntervalSoundComparisonExerciseActivityEntity.class, activities.get(0));
        assertEquals(IntervalTexture.ASCENDING, comparison.getTexture());
        assertEquals(MusicalInterval.PERFECT_4TH, comparison.getFirstInterval());
        assertEquals(MusicalInterval.PERFECT_5TH, comparison.getSecondInterval());
        assertEquals(0, comparison.getPosition());
    }

    @Test
    void activitiesAreWrittenWithTheDiscriminatorTheSeedDataUses() {
        String json = activitiesConverter.convertToDatabaseColumn(
                List.of(new CoolDownRestActivityEntity(2, 100)));

        assertEquals(List.of(2), List.of(
                activitiesConverter.convertToEntityAttribute(json).getFirst().getPosition()));
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"type\":\"COOL_DOWN\""),
                () -> "expected a COOL_DOWN discriminator in " + json);
    }

    @Test
    void stepContextRoundTripsThroughItsColumnFormat() {
        StepContextEntity original = new ListenIntervalContextEntity(
                MusicalInterval.MAJOR_3RD, "UP", IntervalTexture.ASCENDING);

        String json = contextConverter.convertToDatabaseColumn(original);
        StepContextEntity read = contextConverter.convertToEntityAttribute(json);

        var listen = assertInstanceOf(ListenIntervalContextEntity.class, read);
        assertEquals(MusicalInterval.MAJOR_3RD, listen.getInterval());
        assertEquals("UP", listen.getDirection());
        assertEquals(IntervalTexture.ASCENDING, listen.getTexture());
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"type\":\"LISTEN_INTERVAL\""),
                () -> "expected a LISTEN_INTERVAL discriminator in " + json);
    }

    @Test
    void coolDownContextRoundTripsThroughItsColumnFormat() {
        String json = contextConverter.convertToDatabaseColumn(new CoolDownContextEntity(45));

        var read = assertInstanceOf(CoolDownContextEntity.class, contextConverter.convertToEntityAttribute(json));
        assertEquals(45, read.getRestingTimeInSecond());
    }

    @Test
    void nullsAreLeftAsNulls() {
        assertEquals(null, activitiesConverter.convertToDatabaseColumn(null));
        assertEquals(null, activitiesConverter.convertToEntityAttribute(null));
        assertEquals(null, contextConverter.convertToDatabaseColumn(null));
        assertEquals(null, contextConverter.convertToEntityAttribute(null));
    }
}

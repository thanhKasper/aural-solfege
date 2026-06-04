package vn.ktt.ear_training_system.infrastructure.repository.mapper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import vn.ktt.ear_training_system.domain.interval_training.SingleIntervalExerciseFormat;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "formatType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = SingleIntervalExerciseFormat.class, name = "SINGLE_INTERVAL")
})
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class ExerciseFormatMixin {
}

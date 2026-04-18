package vn.ktt.ear_training_system.application.dtos;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PassiveExerciseFormatDTO.class, name = "passive")
})
public sealed interface ExerciseFormatDTO
        permits PassiveExerciseFormatDTO {
    String type();
}

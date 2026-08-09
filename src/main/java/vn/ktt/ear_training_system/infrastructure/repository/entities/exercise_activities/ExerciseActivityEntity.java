package vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SingleIntervalExerciseActivityEntity.class, name = "SINGLE_INTERVAL"),
        @JsonSubTypes.Type(value = CoolDownRestActivityEntity.class, name = "COOL_DOWN")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ExerciseActivityEntity {
    private int position;

    public ExerciseActivityEntity(int position) {
        this.position = position;
    }
}

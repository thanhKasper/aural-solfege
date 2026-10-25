package vn.ktt.eartraining.infrastructure.persistence.entity.activity;

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
        @JsonSubTypes.Type(value = IntervalSoundComparisonExerciseActivityEntity.class, name = "INTERVAL_SOUND_COMPARISON")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ExerciseActivityEntity {
    private int position;

    public ExerciseActivityEntity(int position) {
        this.position = position;
    }
}

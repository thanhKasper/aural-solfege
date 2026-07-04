package vn.ktt.ear_training_system.infrastructure.repository.entities.step_context;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ListenIntervalContextEntity.class, name = "LISTEN_INTERVAL")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class StepContextEntity {
}

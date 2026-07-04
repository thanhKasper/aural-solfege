package vn.ktt.ear_training_system.infrastructure.repository.entities.step_context;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;

@Getter
@Setter
@NoArgsConstructor
public class ListenIntervalContextEntity extends StepContextEntity {
    private MusicalInterval interval;
    private String direction;
    private IntervalTexture texture;

    public ListenIntervalContextEntity(MusicalInterval interval, String direction, IntervalTexture texture) {
        this.interval = interval;
        this.direction = direction;
        this.texture = texture;
    }
}

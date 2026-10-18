package vn.ktt.eartraining.infrastructure.persistence.entity.stepcontext;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.ktt.eartraining.domain.exercise.valueobject.IntervalTexture;
import vn.ktt.eartraining.domain.exercise.valueobject.MusicalInterval;

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

package vn.ktt.ear_training_system.domain.interval_training;

import jakarta.persistence.*;
import vn.ktt.ear_training_system.domain.ExerciseFormat;
import vn.ktt.ear_training_system.domain.TrainingMethodology;

@Entity
@Table(name = "passive_training")
public class SingleIntervalExerciseFormat extends ExerciseFormat {
    @Enumerated(EnumType.STRING)
    @Column(name = "sound_texture")
    private IntervalTexture soundProperty;

    @Enumerated(EnumType.STRING)
    @Column(name = "musical_interval")
    private MusicalInterval interval;

    private int position;

    public SingleIntervalExerciseFormat(TrainingMethodology trainingMethodology, IntervalTexture intervalSoundTexture, MusicalInterval interval, int position) {
        super(trainingMethodology);
        this.interval = interval;
        this.soundProperty = intervalSoundTexture;
        this.position = position;
    }

    public SingleIntervalExerciseFormat() {}

    public IntervalTexture getSoundProperty() {
        return soundProperty;
    }

    public MusicalInterval getInterval() {
        return interval;
    }

    public int getPosition() { return this.position; }
}

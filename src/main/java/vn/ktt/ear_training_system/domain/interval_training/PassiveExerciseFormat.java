package vn.ktt.ear_training_system.domain.interval_training;

import jakarta.persistence.*;
import vn.ktt.ear_training_system.domain.ExerciseFormat;
import vn.ktt.ear_training_system.domain.TrainingMethodology;

@Entity
@Table(name = "passive_training")
public class PassiveExerciseFormat extends ExerciseFormat {
    @Enumerated(EnumType.STRING)
    @Column(name = "sound_texture")
    private IntervalTexture soundProperty;

    @Enumerated(EnumType.STRING)
    @Column(name = "musical_interval")
    private MusicalInterval interval;

    public PassiveExerciseFormat(TrainingMethodology trainingMethodology, IntervalTexture intervalSoundTexture, MusicalInterval interval) {
        super(trainingMethodology);
        this.interval = interval;
        this.soundProperty = intervalSoundTexture;
    }

    public PassiveExerciseFormat() {}

    public IntervalTexture getSoundProperty() {
        return soundProperty;
    }

    public MusicalInterval getInterval() {
        return interval;
    }
}

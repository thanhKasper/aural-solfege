package vn.ktt.ear_training_system.domain.interval_training;

import vn.ktt.ear_training_system.domain.ExerciseFormat;
import vn.ktt.ear_training_system.domain.TrainingMethodology;

public class PassiveExerciseFormat extends ExerciseFormat {
    private final IntervalTexture soundProperty;
    private final MusicalInterval interval;

    public PassiveExerciseFormat(TrainingMethodology trainingMethodology, IntervalTexture intervalSoundTexture, MusicalInterval interval) {
        super(trainingMethodology);
        this.interval = interval;
        this.soundProperty = intervalSoundTexture;
    }

    public IntervalTexture getSoundProperty() {
        return soundProperty;
    }

    public MusicalInterval getInterval() {
        return interval;
    }
}

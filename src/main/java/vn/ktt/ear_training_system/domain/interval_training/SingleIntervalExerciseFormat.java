package vn.ktt.ear_training_system.domain.interval_training;

import vn.ktt.ear_training_system.domain.ExerciseFormat;
import vn.ktt.ear_training_system.domain.TrainingMethodology;

public class SingleIntervalExerciseFormat extends ExerciseFormat {
    private IntervalTexture soundProperty;
    private MusicalInterval interval;
    private int position;

    public SingleIntervalExerciseFormat(TrainingMethodology trainingMethodology, IntervalTexture intervalSoundTexture, MusicalInterval interval, int position) {
        super(trainingMethodology);
        this.interval = interval;
        this.soundProperty = intervalSoundTexture;
        this.position = position;
    }

    protected SingleIntervalExerciseFormat() {
        super(null);
    }

    public IntervalTexture getSoundProperty() {
        return soundProperty;
    }

    public MusicalInterval getInterval() {
        return interval;
    }

    public int getPosition() { return this.position; }
}

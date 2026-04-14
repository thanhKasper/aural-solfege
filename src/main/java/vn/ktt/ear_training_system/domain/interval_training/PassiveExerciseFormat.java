package vn.ktt.ear_training_system.domain.interval_training;

import vn.ktt.ear_training_system.domain.ExerciseFormat;
import vn.ktt.ear_training_system.domain.TrainingMethodology;

public class PassiveExerciseFormat extends ExerciseFormat {
    private final IntervalSoundProperty soundProperty;
    private final MusicalInterval interval;

    public PassiveExerciseFormat(TrainingMethodology trainingMethodology, IntervalSoundProperty intervalProperty, MusicalInterval interval) {
        super(trainingMethodology);
        this.interval = interval;
        this.soundProperty = intervalProperty;
    }

    public IntervalSoundProperty getSoundProperty() {
        return soundProperty;
    }

    public MusicalInterval getInterval() {
        return interval;
    }

    public enum IntervalSoundProperty {
        ASCENDING,
        DESCENDING,
        STACKED
    }

    public enum MusicalInterval {
        UNISON,
        MINOR_2ND,
        MAJOR_2ND,
        MINOR_3RD,
        MAJOR_3RD,
        PERFECT_4TH,
        TRITONE,
        PERFECT_5TH,
        MINOR_6TH,
        MAJOR_6TH,
        MINOR_7TH,
        MAJOR_7TH,
        PERFECT_8TH
    }
}

package vn.ktt.ear_training_system.domain;

import vn.ktt.ear_training_system.domain.interval_training.IntervalTexture;
import vn.ktt.ear_training_system.domain.interval_training.MusicalInterval;
import vn.ktt.ear_training_system.domain.interval_training.SingleIntervalExerciseFormat;

import java.util.List;

public class ExerciseBuilder {
    public ExerciseFormat buildSingleIntervalExerciseFormat(String trainingMethod,
                                                            String interval,
                                                            String intervalProperty, Integer position) {
        return new SingleIntervalExerciseFormat(
                TrainingMethodology.valueOf(trainingMethod),
                IntervalTexture.valueOf(intervalProperty),
                MusicalInterval.valueOf(interval),
                position);
    }

    public Exercise buildExercise(
            String trainingMethod,
            String title,
            String description,
            Integer reps,
            List<ExerciseFormat> exerciseFormats
    ) {
        return new Exercise(TrainingMethodology.valueOf(trainingMethod), title, description, reps, exerciseFormats);
    }
}

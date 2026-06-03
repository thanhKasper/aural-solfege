package vn.ktt.ear_training_system.domain;

import java.util.List;

public class ExerciseBuilder {
    public Exercise buildExercise(
            String trainingMethod,
            String title,
            String description,
            Integer reps,
            Integer rest,
            List<ExerciseFormat> exerciseFormats
    ) {
        return new Exercise(TrainingMethodology.valueOf(trainingMethod), title, description, reps, exerciseFormats);
    }
}

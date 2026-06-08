package vn.ktt.ear_training_system.domain.factory;

import vn.ktt.ear_training_system.domain.Exercise;
import vn.ktt.ear_training_system.domain.ExerciseFormat;
import vn.ktt.ear_training_system.domain.TrainingMethodology;

import java.util.List;

public class ExerciseBuilder {
    public Exercise buildExercise(
            String trainingMethod,
            String title,
            String description,
            boolean loop,
            int repetitions,
            int rest,
            List<ExerciseFormat> exerciseFormats
    ) {
        return new Exercise(TrainingMethodology.valueOf(trainingMethod), title, description, loop, repetitions, rest, exerciseFormats);
    }
}

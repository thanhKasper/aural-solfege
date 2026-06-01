package vn.ktt.ear_training_system.application;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.dtos.ExerciseFormatDTO;
import vn.ktt.ear_training_system.application.dtos.SingleIntervalExerciseFormatDTO;
import vn.ktt.ear_training_system.domain.Exercise;
import vn.ktt.ear_training_system.domain.ExerciseFormat;
import vn.ktt.ear_training_system.domain.interval_training.SingleIntervalExerciseFormat;

@Component
public class ExerciseMapper {
    public ExerciseDTO toExerciseDTO(Exercise exercise) {
        return new ExerciseDTO(
                exercise.getExerciseId(),
                exercise.getTitle(),
                exercise.getDescription(),
                exercise.getTrainingMethodology(),
                exercise.getRepetitions(),
                exercise.getExerciseFormats().stream().map(this::toExerciseFormatDTO).toList(),
                exercise.getRest(),
                exercise.getRepetitions() == null
        );
    }

    public ExerciseFormatDTO toExerciseFormatDTO(ExerciseFormat domain) {
        return switch (domain) {
            case SingleIntervalExerciseFormat p -> mapSingleIntervalExercise(p);
            default -> throw new IllegalStateException("Unexpected value: " + domain);
        };
    }

    private ExerciseFormatDTO mapSingleIntervalExercise(SingleIntervalExerciseFormat exerciseFormat) {
        return new SingleIntervalExerciseFormatDTO(
                exerciseFormat.getInterval().toString(),
                exerciseFormat.getSoundProperty().toString(),
                exerciseFormat.getPosition()
        );
    }
}

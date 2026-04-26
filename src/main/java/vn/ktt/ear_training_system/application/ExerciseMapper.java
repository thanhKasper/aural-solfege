package vn.ktt.ear_training_system.application;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.dtos.ExerciseFormatDTO;
import vn.ktt.ear_training_system.application.dtos.PassiveExerciseFormatDTO;
import vn.ktt.ear_training_system.domain.Exercise;
import vn.ktt.ear_training_system.domain.ExerciseFormat;
import vn.ktt.ear_training_system.domain.interval_training.PassiveExerciseFormat;

@Component
public class ExerciseMapper {
    public ExerciseDTO toExerciseDTO(Exercise exercise) {
        return new ExerciseDTO(
                exercise.getTitle(),
                exercise.getDescription(),
                exercise.getTrainingMethodology(),
                exercise.getRepetitions(),
                exercise.getExerciseFormats().stream().map(this::toExerciseFormatDTO).toList()
        );
    }

    public ExerciseFormatDTO toExerciseFormatDTO(ExerciseFormat domain) {
        return switch (domain) {
            case PassiveExerciseFormat p -> mapPassive(p);
//            case ActiveExerciseFormat a -> mapActive(a);
            default -> throw new IllegalStateException("Unexpected value: " + domain);
        };
    }

    private ExerciseFormatDTO mapPassive(PassiveExerciseFormat exerciseFormat) {
        return new PassiveExerciseFormatDTO(
                exerciseFormat.getInterval().toString(),
                exerciseFormat.getSoundProperty().toString()
        );
    }
}

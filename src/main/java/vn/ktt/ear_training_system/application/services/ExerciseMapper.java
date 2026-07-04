package vn.ktt.ear_training_system.application.services;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.dtos.ExerciseActivityDTO;
import vn.ktt.ear_training_system.application.mappers.exercise_activity.ExerciseActivityDTOToDomainMapperFactory;
import vn.ktt.ear_training_system.domain.exercise.entity.Exercise;
import vn.ktt.ear_training_system.domain.exercise.entity.ExerciseActivity;

@Component
public class ExerciseMapper {
    private final ExerciseActivityDTOToDomainMapperFactory exerciseActivityMapperFactory;

    public ExerciseMapper(ExerciseActivityDTOToDomainMapperFactory exerciseActivityMapperFactory) {
        this.exerciseActivityMapperFactory = exerciseActivityMapperFactory;
    }

    public ExerciseDTO toExerciseDTO(Exercise exercise) {
        return new ExerciseDTO(
                exercise.getExerciseId().toString(),
                exercise.getTitle(),
                exercise.getDescription(),
                exercise.getTrainingMethodology().name(),
                exercise.isLoop() ? null : exercise.getRepetitions(),
                exercise.getExerciseActivities().stream().map(this::toExerciseActivityDTO).toList(),
                exercise.getRest(),
                exercise.isLoop()
        );
    }

    public ExerciseActivityDTO toExerciseActivityDTO(ExerciseActivity domain) {
        return exerciseActivityMapperFactory.toExerciseActivityDTO(domain);
    }

    public ExerciseActivity toDomain(ExerciseActivityDTO dto) {
        return exerciseActivityMapperFactory.toDomain(dto);
    }
}

package vn.ktt.ear_training_system.application.mappers;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.ExerciseActivityDTO;
import vn.ktt.ear_training_system.application.mappers.exercise_activity.ExerciseActivityDTOToDomainMapperFactory;
import vn.ktt.ear_training_system.domain.exercise.entity.Exercise;
import vn.ktt.ear_training_system.domain.exercise.value_object.ExerciseActivity;

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
                exercise.isLoop(),
                exercise.getIntervalNames()
        );
    }

    public ExerciseActivityDTO toExerciseActivityDTO(ExerciseActivity domain) {
        return exerciseActivityMapperFactory.toExerciseActivityDTO(domain);
    }

    public ExerciseActivity toDomain(ExerciseActivityDTO dto) {
        return exerciseActivityMapperFactory.toExerciseActivityDomain(dto);
    }
}

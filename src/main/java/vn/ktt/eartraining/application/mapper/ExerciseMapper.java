package vn.ktt.eartraining.application.mapper;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.application.dto.ExerciseDTO;
import vn.ktt.eartraining.application.dto.activity.ExerciseActivityDTO;
import vn.ktt.eartraining.application.mapper.activity.ExerciseActivityDTOToDomainMapperFactory;
import vn.ktt.eartraining.domain.exercise.entity.Exercise;
import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;

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
                exercise.getRepetitions(),
                exercise.getExerciseActivities().stream().map(this::toExerciseActivityDTO).toList(),
                exercise.getRest(),
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

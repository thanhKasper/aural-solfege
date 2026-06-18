package vn.ktt.ear_training_system.infrastructure.repository.mapper;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.dtos.ExerciseActivityDTO;
import vn.ktt.ear_training_system.domain.exercise.entity.Exercise;
import vn.ktt.ear_training_system.domain.exercise.entity.ExerciseActivity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.ExerciseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExerciseEntityMapper {
    private final Map<Class<?>, IExerciseActivityEntityMapper> exerciseActivityMapperMap = new HashMap<>();

    public ExerciseEntityMapper(List<IExerciseActivityEntityMapper> exerciseActivityMappers) {
        for (IExerciseActivityEntityMapper exerciseActivityMapper : exerciseActivityMappers) {
            exerciseActivityMapperMap.put(
                    exerciseActivityMapper.getExerciseActivityEntityClass(),
                    exerciseActivityMapper);
        }
    }

    public ExerciseEntity toEntity(Exercise domain) {
        var entity = new ExerciseEntity();
        entity.setExerciseId(domain.getExerciseId());
        entity.setTrainingMethodology(domain.getTrainingMethodology());
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setRepetitions(domain.isLoop() ? null : domain.getRepetitions());
        entity.setRest(domain.getRest());
        entity.setExerciseActivities(domain.getExerciseActivities());
        return entity;
    }

    public Exercise toDomain(ExerciseEntity entity) {
        var entityRepetitions = entity.getRepetitions();
        return new Exercise(
                entity.getExerciseId(),
                entity.getTrainingMethodology(),
                entity.getTitle(),
                entity.getDescription(),
                entityRepetitions == null,
                entityRepetitions == null ? 0 : entityRepetitions,
                entity.getRest(),
                entity.getExerciseActivities()
        );
    }

    public ExerciseDTO toDto(ExerciseEntity entity) {
        List<ExerciseActivityDTO> exerciseActivityDTOs = entity
                .getExerciseActivities().stream().map(this::toExerciseActivityDTO).toList();

        return new ExerciseDTO(
                entity.getExerciseId().toString(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getTrainingMethodology().name(),
                entity.getRepetitions(),
                exerciseActivityDTOs,
                entity.getRest(),
                entity.getRepetitions() == null
        );
    }

    private ExerciseActivityDTO toExerciseActivityDTO(ExerciseActivity exerciseActivity) {
        return getMapper(exerciseActivity.getClass()).toExerciseActivityDTO(exerciseActivity);
    }

    private IExerciseActivityEntityMapper getMapper(Class<?> exerciseActivityClass) {
        if (exerciseActivityMapperMap.containsKey(exerciseActivityClass)) {
            return exerciseActivityMapperMap.get(exerciseActivityClass);
        }
        else {
            throw new IllegalArgumentException("No mapper for " + exerciseActivityClass);
        }
    }
}

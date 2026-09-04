package vn.ktt.ear_training_system.infrastructure.repository.mapper;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.exercise.entity.Exercise;
import vn.ktt.ear_training_system.domain.exercise.value_object.ExerciseActivity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities.ExerciseActivityEntity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.ExerciseEntity;
import vn.ktt.ear_training_system.infrastructure.repository.mapper.exercise_activity.ExerciseActivityEntityToDomainMapperFactory;

import java.util.List;

@Component
public class ExerciseEntityMapper {
    private final ExerciseActivityEntityToDomainMapperFactory exerciseActivityEntityToDomainMapperFactory;

    public ExerciseEntityMapper(ExerciseActivityEntityToDomainMapperFactory exerciseActivityEntityToDomainMapperFactory) {
        this.exerciseActivityEntityToDomainMapperFactory = exerciseActivityEntityToDomainMapperFactory;
    }

    public ExerciseEntity toEntity(Exercise domain) {
        var entity = new ExerciseEntity();
        entity.setExerciseId(domain.getExerciseId());
        entity.setTrainingMethodology(domain.getTrainingMethodology());
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setRepetitions(domain.isLoop() ? null : domain.getRepetitions());
        entity.setRest(domain.getRest());
        entity.setExerciseActivities(toEntityActivities(domain.getExerciseActivities()));
        return entity;
    }

    public Exercise toDomain(ExerciseEntity entity) {
        var entityRepetitions = entity.getRepetitions();
        return Exercise.reconstruct(
                entity.getExerciseId(),
                entity.getTrainingMethodology(),
                entity.getTitle(),
                entity.getDescription(),
                entityRepetitions == null,
                entityRepetitions == null ? 0 : entityRepetitions,
                entity.getRest(),
                toDomainActivities(entity.getExerciseActivities())
        );
    }

    private List<ExerciseActivityEntity> toEntityActivities(List<ExerciseActivity> activities) {
        return activities.stream()
                .map(exerciseActivityEntityToDomainMapperFactory::toExerciseActivityEntity)
                .toList();
    }

    private List<ExerciseActivity> toDomainActivities(List<ExerciseActivityEntity> entities) {
        return entities.stream()
                .map(exerciseActivityEntityToDomainMapperFactory::toExerciseActivity)
                .toList();
    }
}

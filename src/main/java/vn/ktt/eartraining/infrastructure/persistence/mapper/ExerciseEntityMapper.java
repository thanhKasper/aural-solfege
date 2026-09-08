package vn.ktt.eartraining.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.domain.exercise.entity.Exercise;
import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.infrastructure.persistence.entity.activity.ExerciseActivityEntity;
import vn.ktt.eartraining.infrastructure.persistence.entity.ExerciseEntity;
import vn.ktt.eartraining.infrastructure.persistence.mapper.activity.ExerciseActivityEntityToDomainMapperFactory;

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

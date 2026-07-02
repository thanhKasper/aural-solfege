package vn.ktt.ear_training_system.infrastructure.repository.mapper;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.exercise.entity.Exercise;
import vn.ktt.ear_training_system.domain.exercise.entity.ExerciseActivity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities.ExerciseActivityEntity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.ExerciseEntity;
import vn.ktt.ear_training_system.infrastructure.repository.mapper.exercise_activity.ExerciseActivityMapperRegistry;

import java.util.List;

@Component
public class ExerciseEntityMapper {
    private final ExerciseActivityMapperRegistry exerciseActivityMapperRegistry;

    public ExerciseEntityMapper(ExerciseActivityMapperRegistry exerciseActivityMapperRegistry) {
        this.exerciseActivityMapperRegistry = exerciseActivityMapperRegistry;
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
        return new Exercise(
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
                .map(exerciseActivityMapperRegistry::toExerciseActivityEntity)
                .toList();
    }

    private List<ExerciseActivity> toDomainActivities(List<ExerciseActivityEntity> entities) {
        return entities.stream()
                .map(exerciseActivityMapperRegistry::toExerciseActivity)
                .toList();
    }
}

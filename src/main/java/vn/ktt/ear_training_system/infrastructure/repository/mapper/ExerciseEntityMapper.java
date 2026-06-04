package vn.ktt.ear_training_system.infrastructure.repository.mapper;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.Exercise;
import vn.ktt.ear_training_system.infrastructure.repository.entities.ExerciseEntity;

@Component
public class ExerciseEntityMapper {

    public ExerciseEntity toEntity(Exercise domain) {
        var entity = new ExerciseEntity();
        entity.setExerciseId(domain.getExerciseUuid());
        entity.setTrainingMethodology(domain.getTrainingMethodologyEnum());
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setRepetitions(domain.getRepetitions());
        entity.setRest(domain.getRest());
        entity.setExerciseFormats(domain.getExerciseFormats());
        return entity;
    }

    public Exercise toDomain(ExerciseEntity entity) {
        return new Exercise(
                entity.getExerciseId(),
                entity.getTrainingMethodology(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getRepetitions(),
                entity.getRest(),
                entity.getExerciseFormats()
        );
    }
}

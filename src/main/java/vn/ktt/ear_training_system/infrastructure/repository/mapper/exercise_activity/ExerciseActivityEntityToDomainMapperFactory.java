package vn.ktt.ear_training_system.infrastructure.repository.mapper.exercise_activity;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.exercise.value_object.ExerciseActivity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities.ExerciseActivityEntity;
import vn.ktt.shared.DataMapperRegistry;

import java.util.List;

@Component
public class ExerciseActivityEntityToDomainMapperFactory extends DataMapperRegistry<ExerciseActivity, ExerciseActivityEntity, IExerciseActivityEntityToDomainMapper> {
    public ExerciseActivityEntityToDomainMapperFactory(List<IExerciseActivityEntityToDomainMapper> exerciseActivityMappers) {
        super(exerciseActivityMappers);
    }

    public ExerciseActivity toExerciseActivity(ExerciseActivityEntity entity) {
        var mapper = getMapperBaseOnDataTo(entity);
        if (mapper != null) {
            return mapper.reverseTransform(entity);
        }

        throw new IllegalArgumentException("Missing mapper to convert entity " + entity.getClass());
    }

    public ExerciseActivityEntity toExerciseActivityEntity(ExerciseActivity domain) {
        var mapper = getMapperBaseOnDataFrom(domain);
        if (mapper != null) {
            return mapper.transform(domain);
        }

        throw new IllegalArgumentException("Missing mapper to convert entity " + domain.getClass());
    }
}

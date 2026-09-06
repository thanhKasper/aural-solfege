package vn.ktt.ear_training_system.infrastructure.repository.mapper.exercise_activity;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.exercise.value_object.ExerciseActivity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities.ExerciseActivityEntity;
import vn.ktt.shared.DataMapperRegistry;

import java.util.List;

@Component
public class ExerciseActivityEntityToDomainMapperFactory extends DataMapperRegistry<ExerciseActivityEntityMapperKey, ExerciseActivity, ExerciseActivityEntity> {

    public ExerciseActivityEntityToDomainMapperFactory(List<IExerciseActivityEntityToDomainMapper> exerciseActivityMappers) {
        super(exerciseActivityMappers);
    }

    public ExerciseActivity toExerciseActivity(ExerciseActivityEntity entity) {
        return this.reverseTransform(entity);
    }

    public ExerciseActivity toExerciseActivity(ExerciseActivityEntity entity, Class<? extends ExerciseActivity> sourceType) {
        return this.reverseTransform(entity, sourceType);
    }

    public ExerciseActivityEntity toExerciseActivityEntity(ExerciseActivity domain) {
        return this.transform(domain);
    }

    public ExerciseActivityEntity toExerciseActivityEntity(ExerciseActivity domain, Class<? extends ExerciseActivityEntity> targetType) {
        return this.transform(domain, targetType);
    }
}
package vn.ktt.eartraining.infrastructure.persistence.mapper.activity;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.infrastructure.persistence.entity.activity.ExerciseActivityEntity;
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
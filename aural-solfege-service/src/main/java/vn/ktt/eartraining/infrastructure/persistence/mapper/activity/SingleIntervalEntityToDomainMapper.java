package vn.ktt.eartraining.infrastructure.persistence.mapper.activity;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.exercise.valueobject.activity.SingleIntervalExerciseActivity;
import vn.ktt.eartraining.infrastructure.persistence.entity.activity.ExerciseActivityEntity;
import vn.ktt.eartraining.infrastructure.persistence.entity.activity.SingleIntervalExerciseActivityEntity;

@Component
public class SingleIntervalEntityToDomainMapper implements IExerciseActivityEntityToDomainMapper {

    @Override
    public Class<? extends ExerciseActivity> getDataFromClass() {
        return SingleIntervalExerciseActivity.class;
    }

    @Override
    public Class<? extends ExerciseActivityEntity> getDataToClass() {
        return SingleIntervalExerciseActivityEntity.class;
    }

    @Override
    public ExerciseActivityEntityMapperKey getKey() {
        return ExerciseActivityEntityMapperKey.SINGLE_INTERVAL;
    }

    @Override
    public ExerciseActivityEntity transform(ExerciseActivity dataFrom) {
        var activity = (SingleIntervalExerciseActivity) dataFrom;
        return new SingleIntervalExerciseActivityEntity(
                activity.getSoundProperty(),
                activity.getIntervals(),
                activity.getPosition()
        );
    }

    @Override
    public ExerciseActivity reverseTransform(ExerciseActivityEntity dataTo) {
        var entity = (SingleIntervalExerciseActivityEntity) dataTo;
        return new SingleIntervalExerciseActivity(
                entity.getSoundProperty(),
                entity.getIntervals(),
                entity.getPosition()
        );
    }
}

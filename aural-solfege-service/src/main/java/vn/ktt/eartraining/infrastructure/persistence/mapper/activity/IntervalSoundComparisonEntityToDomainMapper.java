package vn.ktt.eartraining.infrastructure.persistence.mapper.activity;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.exercise.valueobject.activity.IntervalSoundComparison;
import vn.ktt.eartraining.infrastructure.persistence.entity.activity.ExerciseActivityEntity;
import vn.ktt.eartraining.infrastructure.persistence.entity.activity.IntervalSoundComparisonExerciseActivityEntity;

@Component
public class IntervalSoundComparisonEntityToDomainMapper implements IExerciseActivityEntityToDomainMapper {

    @Override
    public Class<? extends ExerciseActivity> getDataFromClass() {
        return IntervalSoundComparison.class;
    }

    @Override
    public Class<? extends ExerciseActivityEntity> getDataToClass() {
        return IntervalSoundComparisonExerciseActivityEntity.class;
    }

    @Override
    public ExerciseActivityEntityMapperKey getKey() {
        return ExerciseActivityEntityMapperKey.INTERVAL_SOUND_COMPARISON;
    }

    @Override
    public ExerciseActivityEntity transform(ExerciseActivity dataFrom) {
        var activity = (IntervalSoundComparison) dataFrom;
        return new IntervalSoundComparisonExerciseActivityEntity(
                activity.getTexture(),
                activity.getFirstInterval(),
                activity.getSecondInterval(),
                activity.getPosition()
        );
    }

    @Override
    public ExerciseActivity reverseTransform(ExerciseActivityEntity dataTo) {
        var entity = (IntervalSoundComparisonExerciseActivityEntity) dataTo;
        return IntervalSoundComparison.construct(
                entity.getPosition(),
                entity.getTexture(),
                entity.getFirstInterval(),
                entity.getSecondInterval()
        );
    }
}

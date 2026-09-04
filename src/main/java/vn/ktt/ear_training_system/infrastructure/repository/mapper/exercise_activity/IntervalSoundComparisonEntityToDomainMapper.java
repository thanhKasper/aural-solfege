package vn.ktt.ear_training_system.infrastructure.repository.mapper.exercise_activity;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.exercise.value_object.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.value_object.exercise_activity.IntervalSoundComparison;
import vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities.ExerciseActivityEntity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities.IntervalSoundComparisonExerciseActivityEntity;

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

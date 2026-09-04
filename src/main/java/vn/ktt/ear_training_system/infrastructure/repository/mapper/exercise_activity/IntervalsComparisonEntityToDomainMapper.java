package vn.ktt.ear_training_system.infrastructure.repository.mapper.exercise_activity;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.exercise.value_object.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.value_object.exercise_activity.IntervalsComparison;
import vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities.ExerciseActivityEntity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities.IntervalsComparisonExerciseActivityEntity;

@Component
public class IntervalsComparisonEntityToDomainMapper implements IExerciseActivityEntityToDomainMapper {

    @Override
    public Class<? extends ExerciseActivity> getDataFromClass() {
        return IntervalsComparison.class;
    }

    @Override
    public Class<? extends ExerciseActivityEntity> getDataToClass() {
        return IntervalsComparisonExerciseActivityEntity.class;
    }

    @Override
    public ExerciseActivityEntity transform(ExerciseActivity dataFrom) {
        var activity = (IntervalsComparison) dataFrom;
        return new IntervalsComparisonExerciseActivityEntity(
                activity.getTexture(),
                activity.getFirstInterval(),
                activity.getSecondInterval(),
                activity.getPosition()
        );
    }

    @Override
    public ExerciseActivity reverseTransform(ExerciseActivityEntity dataTo) {
        var entity = (IntervalsComparisonExerciseActivityEntity) dataTo;
        return IntervalsComparison.construct(
                entity.getPosition(),
                entity.getTexture(),
                entity.getFirstInterval(),
                entity.getSecondInterval()
        );
    }
}

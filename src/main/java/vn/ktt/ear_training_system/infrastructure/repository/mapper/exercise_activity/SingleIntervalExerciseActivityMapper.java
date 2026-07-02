package vn.ktt.ear_training_system.infrastructure.repository.mapper.exercise_activity;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.exercise.entity.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.entity.interval_training.SingleIntervalExerciseActivity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities.ExerciseActivityEntity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities.SingleIntervalExerciseActivityEntity;

@Component
public class SingleIntervalExerciseActivityMapper implements IExerciseActivityMapper {

    @Override
    public Class<? extends ExerciseActivity> getDataFromClass() {
        return SingleIntervalExerciseActivity.class;
    }

    @Override
    public Class<? extends ExerciseActivityEntity> getDataToClass() {
        return SingleIntervalExerciseActivityEntity.class;
    }

    @Override
    public ExerciseActivityEntity transform(ExerciseActivity dataFrom) {
        var activity = (SingleIntervalExerciseActivity) dataFrom;
        return new SingleIntervalExerciseActivityEntity(
                activity.getSoundProperty(),
                activity.getInterval(),
                activity.getPosition()
        );
    }

    @Override
    public ExerciseActivity reverseTransform(ExerciseActivityEntity dataTo) {
        var entity = (SingleIntervalExerciseActivityEntity) dataTo;
        return new SingleIntervalExerciseActivity(
                entity.getSoundProperty(),
                entity.getInterval(),
                entity.getPosition()
        );
    }
}

package vn.ktt.ear_training_system.infrastructure.repository.mapper.exercise_activity;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.exercise.value_object.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.value_object.exercise_activity.CoolDownRestActivity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities.CoolDownRestActivityEntity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities.ExerciseActivityEntity;

@Component
public class CoolDownActivityEntityToDomainMapper implements IExerciseActivityEntityToDomainMapper {
    @Override
    public Class<? extends ExerciseActivity> getDataFromClass() {
        return CoolDownRestActivity.class;
    }

    @Override
    public Class<? extends ExerciseActivityEntity> getDataToClass() {
        return CoolDownRestActivityEntity.class;
    }

    @Override
    public ExerciseActivityEntity transform(ExerciseActivity exerciseActivity) {
        CoolDownRestActivity restingActivity = (CoolDownRestActivity) exerciseActivity;
        return new CoolDownRestActivityEntity(restingActivity.getPosition(), restingActivity.getRestAmountInSecond());
    }

    @Override
    public ExerciseActivity reverseTransform(ExerciseActivityEntity exerciseActivityEntity) {
        CoolDownRestActivityEntity restingEntity = (CoolDownRestActivityEntity) exerciseActivityEntity;
        return new CoolDownRestActivity(restingEntity.getPosition(), restingEntity.getRestAmountInSecond());
    }
}

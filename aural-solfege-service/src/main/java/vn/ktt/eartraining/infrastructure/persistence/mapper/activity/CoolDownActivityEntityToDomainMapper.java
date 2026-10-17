package vn.ktt.eartraining.infrastructure.persistence.mapper.activity;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.exercise.valueobject.activity.CoolDownRestActivity;
import vn.ktt.eartraining.infrastructure.persistence.entity.activity.CoolDownRestActivityEntity;
import vn.ktt.eartraining.infrastructure.persistence.entity.activity.ExerciseActivityEntity;

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
    public ExerciseActivityEntityMapperKey getKey() {
        return ExerciseActivityEntityMapperKey.COOL_DOWN;
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

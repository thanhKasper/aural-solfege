package vn.ktt.eartraining.infrastructure.persistence.mapper.activity;

import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.infrastructure.persistence.entity.activity.ExerciseActivityEntity;
import vn.ktt.shared.IDataMapper;

public interface IExerciseActivityEntityToDomainMapper extends IDataMapper<ExerciseActivityEntityMapperKey, ExerciseActivity, ExerciseActivityEntity> {
}
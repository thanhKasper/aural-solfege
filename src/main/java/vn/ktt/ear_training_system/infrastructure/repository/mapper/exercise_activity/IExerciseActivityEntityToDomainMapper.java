package vn.ktt.ear_training_system.infrastructure.repository.mapper.exercise_activity;

import vn.ktt.ear_training_system.domain.exercise.value_object.ExerciseActivity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.exercise_activities.ExerciseActivityEntity;
import vn.ktt.shared.IDataMapper;

public interface IExerciseActivityEntityToDomainMapper extends IDataMapper<ExerciseActivity, ExerciseActivityEntity> {
}

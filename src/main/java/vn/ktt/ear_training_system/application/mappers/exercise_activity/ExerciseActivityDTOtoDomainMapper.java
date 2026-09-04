package vn.ktt.ear_training_system.application.mappers.exercise_activity;

import vn.ktt.ear_training_system.application.dtos.exercise_activities.ExerciseActivityDTO;
import vn.ktt.ear_training_system.domain.exercise.value_object.ExerciseActivity;
import vn.ktt.shared.IDataMapper;

public interface ExerciseActivityDTOtoDomainMapper extends IDataMapper<ExerciseActivity, ExerciseActivityDTO> {
}

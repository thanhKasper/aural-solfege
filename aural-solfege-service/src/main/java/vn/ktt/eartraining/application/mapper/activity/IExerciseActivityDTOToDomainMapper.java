package vn.ktt.eartraining.application.mapper.activity;

import vn.ktt.eartraining.application.dto.activity.ExerciseActivityDTO;
import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.shared.IDataMapper;

public interface IExerciseActivityDTOToDomainMapper extends IDataMapper<ExerciseActivityMapperKey, ExerciseActivity, ExerciseActivityDTO> {
}
package vn.ktt.ear_training_system.infrastructure.repository.mapper;

import vn.ktt.ear_training_system.application.dtos.ExerciseFormatDTO;
import vn.ktt.ear_training_system.domain.ExerciseActivity;

public interface IExerciseFormatEntityMapper {
    Class<? extends ExerciseActivity> getExerciseFormatEntityClass();
    ExerciseFormatDTO toExerciseFormatDTO(ExerciseActivity domain);
}

package vn.ktt.ear_training_system.infrastructure.repository.mapper;

import vn.ktt.ear_training_system.application.dtos.ExerciseActivityDTO;
import vn.ktt.ear_training_system.domain.ExerciseActivity;

public interface IExerciseActivityEntityMapper {
    Class<? extends ExerciseActivity> getExerciseActivityEntityClass();
    ExerciseActivityDTO toExerciseActivityDTO(ExerciseActivity domain);
}

package vn.ktt.ear_training_system.infrastructure.repository.mapper;

import vn.ktt.ear_training_system.application.dtos.ExerciseFormatDTO;
import vn.ktt.ear_training_system.domain.ExerciseFormat;

public interface IExerciseFormatEntityMapper {
    Class<? extends ExerciseFormat> getExerciseFormatEntityClass();
    ExerciseFormatDTO toExerciseFormatDTO(ExerciseFormat domain);
}

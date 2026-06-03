package vn.ktt.ear_training_system.application;

import vn.ktt.ear_training_system.application.dtos.ExerciseFormatDTO;
import vn.ktt.ear_training_system.domain.ExerciseFormat;
import vn.ktt.ear_training_system.domain.TrainingMethodology;

public interface ExerciseFormatMapper {
    Class<? extends ExerciseFormat> getDomainClass();
    Class<? extends ExerciseFormatDTO> getDtoClass();
    ExerciseFormatDTO toDto(ExerciseFormat domain);
    ExerciseFormat toDomain(ExerciseFormatDTO dto, TrainingMethodology trainingMethodology);
}

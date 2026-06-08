package vn.ktt.ear_training_system.application.format_mappers;

import vn.ktt.ear_training_system.application.dtos.ExerciseFormatDTO;
import vn.ktt.ear_training_system.domain.ExerciseFormat;

public interface ExerciseFormatMapper {
    Class<? extends ExerciseFormat> getDomainClass();
    Class<? extends ExerciseFormatDTO> getDtoClass();
    ExerciseFormatDTO toDto(ExerciseFormat domain);
    ExerciseFormat toDomain(ExerciseFormatDTO dto);
}

package vn.ktt.ear_training_system.application.format_mappers;

import vn.ktt.ear_training_system.application.dtos.ExerciseFormatDTO;
import vn.ktt.ear_training_system.domain.ExerciseActivity;

public interface ExerciseFormatMapper {
    Class<? extends ExerciseActivity> getDomainClass();
    Class<? extends ExerciseFormatDTO> getDtoClass();
    ExerciseFormatDTO toDto(ExerciseActivity domain);
    ExerciseActivity toDomain(ExerciseFormatDTO dto);
}

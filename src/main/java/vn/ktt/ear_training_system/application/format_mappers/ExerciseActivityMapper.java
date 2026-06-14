package vn.ktt.ear_training_system.application.format_mappers;

import vn.ktt.ear_training_system.application.dtos.ExerciseActivityDTO;
import vn.ktt.ear_training_system.domain.ExerciseActivity;

public interface ExerciseActivityMapper {
    Class<? extends ExerciseActivity> getDomainClass();
    Class<? extends ExerciseActivityDTO> getDtoClass();
    ExerciseActivityDTO toDto(ExerciseActivity domain);
    ExerciseActivity toDomain(ExerciseActivityDTO dto);
}

package vn.ktt.ear_training_system.application.format_mappers;

import vn.ktt.ear_training_system.application.dtos.PracticeStepDTO;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeStep;

public interface PracticeStepMapper {
    Class<? extends PracticeStep> getDomainClass();
    Class<? extends PracticeStepDTO> getDtoClass();
    PracticeStepDTO toDto(PracticeStep domain);
    PracticeStep toDomain(PracticeStepDTO dto);
}

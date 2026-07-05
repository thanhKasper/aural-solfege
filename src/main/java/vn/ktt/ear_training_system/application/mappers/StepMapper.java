package vn.ktt.ear_training_system.application.mappers;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.PracticeStepDTO;
import vn.ktt.ear_training_system.application.mappers.practice_step.PracticeStepDTOToDomainMapperFactory;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeStep;

@Component
public class StepMapper {
    private final PracticeStepDTOToDomainMapperFactory practiceStepMapperFactory;

    public StepMapper(PracticeStepDTOToDomainMapperFactory practiceStepMapperFactory) {
        this.practiceStepMapperFactory = practiceStepMapperFactory;
    }

    public PracticeStepDTO toDto(PracticeStep domain) {
        return practiceStepMapperFactory.toDto(domain);
    }

    public PracticeStep toDomain(PracticeStepDTO dto) {
        return practiceStepMapperFactory.toDomain(dto);
    }
}

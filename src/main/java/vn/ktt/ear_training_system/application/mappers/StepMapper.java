package vn.ktt.ear_training_system.application.mappers;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.practice_step.PracticeStepDTO;
import vn.ktt.ear_training_system.application.mappers.step_context.StepContextDomainToDTOMapperFactory;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeStep;

@Component
public class StepMapper {
    private final StepContextDomainToDTOMapperFactory stepContextMapperFactory;

    public StepMapper(StepContextDomainToDTOMapperFactory stepContextMapperFactory) {
        this.stepContextMapperFactory = stepContextMapperFactory;
    }

    public PracticeStepDTO toDTO(PracticeStep domain) {
        PracticeStepDTO dto = stepContextMapperFactory.toDto(domain.getContext());
        dto.setActivityPosition(domain.getActivityPosition());
        dto.setStatus(domain.getStatus().toString());

        return dto;
    }
}

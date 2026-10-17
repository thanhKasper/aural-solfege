package vn.ktt.eartraining.application.mapper;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.application.dto.step.PracticeStepDTO;
import vn.ktt.eartraining.application.mapper.stepcontext.StepContextDomainToDTOMapperFactory;
import vn.ktt.eartraining.domain.session.entity.PracticeStep;

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

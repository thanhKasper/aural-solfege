package vn.ktt.eartraining.application.mapper.stepcontext;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.application.dto.step.PracticeStepDTO;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.StepContext;
import vn.ktt.shared.DataMapperRegistry;
import vn.ktt.shared.IDataMapper;

import java.util.List;

@Component
public class StepContextDomainToDTOMapperFactory extends DataMapperRegistry<StepContextMapperKey, StepContext, PracticeStepDTO> {

    public StepContextDomainToDTOMapperFactory(List<IDataMapper<StepContextMapperKey, StepContext, PracticeStepDTO>> stepContextMappers) {
        super(stepContextMappers);
    }

    public PracticeStepDTO toDto(StepContext domain) {
        return this.transform(domain);
    }

    public PracticeStepDTO toDto(StepContext domain, Class<? extends PracticeStepDTO> targetType) {
        return this.transform(domain, targetType);
    }

    public StepContext toDomain(PracticeStepDTO dto) {
        return this.reverseTransform(dto);
    }

    public StepContext toDomain(PracticeStepDTO dto, Class<? extends StepContext> sourceType) {
        return this.reverseTransform(dto, sourceType);
    }
}
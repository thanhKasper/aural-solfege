package vn.ktt.ear_training_system.application.mappers.step_context;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.practice_step.PracticeStepDTO;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.StepContext;
import vn.ktt.shared.DataMapperRegistry;

import java.util.List;

@Component
public class StepContextDomainToDTOMapperFactory extends DataMapperRegistry<StepContext, PracticeStepDTO, StepContextMapper> {
    public StepContextDomainToDTOMapperFactory(List<StepContextMapper> stepContextMappers) {
        super(stepContextMappers);
    }

    public PracticeStepDTO toDto(StepContext domain) {
        var mapper = getMapperBaseOnDataFrom(domain);
        if (mapper == null) {
            throw new IllegalArgumentException("No mapper for " + domain.getClass());
        }
        return mapper.transform(domain);
    }

    public StepContext toDomain(PracticeStepDTO dto) {
        var mapper = getMapperBaseOnDataTo(dto);
        if (mapper == null) {
            throw new IllegalArgumentException("No mapper for step: " + dto.getClass().getSimpleName());
        }
        return mapper.reverseTransform(dto);
    }
}

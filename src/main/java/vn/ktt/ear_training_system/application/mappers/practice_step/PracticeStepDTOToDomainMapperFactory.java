package vn.ktt.ear_training_system.application.mappers.practice_step;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.PracticeStepDTO;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeStep;
import vn.ktt.shared.DataMapperRegistry;

import java.util.List;

@Component
public class PracticeStepDTOToDomainMapperFactory extends DataMapperRegistry<PracticeStep, PracticeStepDTO, PracticeStepMapper> {
    public PracticeStepDTOToDomainMapperFactory(List<PracticeStepMapper> practiceStepMappers) {
        super(practiceStepMappers);
    }

    public PracticeStepDTO toDto(PracticeStep domain) {
        var mapper = getMapperBaseOnDataFrom(domain);
        if (mapper == null) {
            throw new IllegalArgumentException("No mapper for " + domain.getClass());
        }
        return mapper.transform(domain);
    }

    public PracticeStep toDomain(PracticeStepDTO dto) {
        var mapper = getMapperBaseOnDataTo(dto);
        if (mapper == null) {
            throw new IllegalArgumentException("No mapper for step: " + dto.getClass().getSimpleName());
        }
        return mapper.reverseTransform(dto);
    }
}

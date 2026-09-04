package vn.ktt.ear_training_system.infrastructure.repository.mapper.step_context;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.StepContext;
import vn.ktt.ear_training_system.infrastructure.repository.entities.step_context.StepContextEntity;
import vn.ktt.shared.DataMapperRegistry;

import java.util.List;

@Component
public class StepContextEntityToDomainMapperFactory extends DataMapperRegistry<StepContext, StepContextEntity, IStepContextEntityToDomainMapper> {
    public StepContextEntityToDomainMapperFactory(List<IStepContextEntityToDomainMapper> mappers) {
        super(mappers);
    }

    public StepContextEntity toStepContextEntity(StepContext domain) {
        var mapper = getMapperBaseOnDataFrom(domain);
        if (mapper == null) {
            throw new IllegalArgumentException("Missing mapper for " + domain.getClass());
        }
        return mapper.transform(domain);
    }

    public StepContext toStepContext(StepContextEntity entity) {
        var mapper = getMapperBaseOnDataTo(entity);
        if (mapper == null) {
            throw new IllegalArgumentException("Missing mapper for entity " + entity.getClass());
        }
        return mapper.reverseTransform(entity);
    }
}

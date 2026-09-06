package vn.ktt.ear_training_system.infrastructure.repository.mapper.step_context;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.StepContext;
import vn.ktt.ear_training_system.infrastructure.repository.entities.step_context.StepContextEntity;
import vn.ktt.shared.DataMapperRegistry;

import java.util.List;

@Component
public class StepContextEntityToDomainMapperFactory extends DataMapperRegistry<StepContextEntityMapperKey, StepContext, StepContextEntity> {

    public StepContextEntityToDomainMapperFactory(List<IStepContextEntityToDomainMapper> mappers) {
        super(mappers);
    }

    public StepContextEntity toStepContextEntity(StepContext domain) {
        return this.transform(domain);
    }

    public StepContextEntity toStepContextEntity(StepContext domain, Class<? extends StepContextEntity> targetType) {
        return this.transform(domain, targetType);
    }

    public StepContext toStepContext(StepContextEntity entity) {
        return this.reverseTransform(entity);
    }

    public StepContext toStepContext(StepContextEntity entity, Class<? extends StepContext> sourceType) {
        return this.reverseTransform(entity, sourceType);
    }
}
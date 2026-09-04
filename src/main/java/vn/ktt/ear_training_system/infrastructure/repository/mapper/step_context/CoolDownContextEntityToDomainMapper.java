package vn.ktt.ear_training_system.infrastructure.repository.mapper.step_context;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.CoolDownContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.StepContext;
import vn.ktt.ear_training_system.infrastructure.repository.entities.step_context.CoolDownContextEntity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.step_context.StepContextEntity;

@Component
public class CoolDownContextEntityToDomainMapper implements IStepContextEntityToDomainMapper {

    @Override
    public Class<? extends StepContext> getDataFromClass() {
        return CoolDownContext.class;
    }

    @Override
    public Class<? extends StepContextEntity> getDataToClass() {
        return CoolDownContextEntity.class;
    }

    @Override
    public StepContextEntity transform(StepContext dataFrom) {
        var ctx = (CoolDownContext) dataFrom;
        return new CoolDownContextEntity(ctx.restingTimeInSecond());
    }

    @Override
    public StepContext reverseTransform(StepContextEntity dataTo) {
        var entity = (CoolDownContextEntity) dataTo;
        return new CoolDownContext(entity.getRestingTimeInSecond());
    }
}

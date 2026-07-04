package vn.ktt.ear_training_system.infrastructure.repository.mapper.step_context;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.practice_session.value_object.ListenIntervalContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepContext;
import vn.ktt.ear_training_system.infrastructure.repository.entities.step_context.ListenIntervalContextEntity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.step_context.StepContextEntity;

@Component
public class ListenIntervalContextEntityToDomainMapper implements IStepContextEntityToDomainMapper {

    @Override
    public Class<? extends StepContext> getDataFromClass() {
        return ListenIntervalContext.class;
    }

    @Override
    public Class<? extends StepContextEntity> getDataToClass() {
        return ListenIntervalContextEntity.class;
    }

    @Override
    public StepContextEntity transform(StepContext dataFrom) {
        var ctx = (ListenIntervalContext) dataFrom;
        return new ListenIntervalContextEntity(ctx.interval(), ctx.direction(), ctx.texture());
    }

    @Override
    public StepContext reverseTransform(StepContextEntity dataTo) {
        var entity = (ListenIntervalContextEntity) dataTo;
        return new ListenIntervalContext(entity.getInterval(), entity.getDirection(), entity.getTexture());
    }
}

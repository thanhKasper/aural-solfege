package vn.ktt.eartraining.infrastructure.persistence.mapper.stepcontext;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.ListenIntervalContext;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.StepContext;
import vn.ktt.eartraining.infrastructure.persistence.entity.stepcontext.ListenIntervalContextEntity;
import vn.ktt.eartraining.infrastructure.persistence.entity.stepcontext.StepContextEntity;

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
    public StepContextEntityMapperKey getKey() {
        return StepContextEntityMapperKey.SINGLE_INTERVAL;
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

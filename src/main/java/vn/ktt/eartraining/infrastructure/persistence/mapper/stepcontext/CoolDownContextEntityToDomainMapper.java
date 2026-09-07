package vn.ktt.eartraining.infrastructure.persistence.mapper.stepcontext;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.CoolDownContext;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.StepContext;
import vn.ktt.eartraining.infrastructure.persistence.entity.stepcontext.CoolDownContextEntity;
import vn.ktt.eartraining.infrastructure.persistence.entity.stepcontext.StepContextEntity;

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
    public StepContextEntityMapperKey getKey() {
        return StepContextEntityMapperKey.COOL_DOWN;
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

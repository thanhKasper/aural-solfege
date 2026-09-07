package vn.ktt.eartraining.infrastructure.persistence.mapper.stepcontext;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.IntervalSoundComparisonContext;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.StepContext;
import vn.ktt.eartraining.infrastructure.persistence.entity.stepcontext.IntervalSoundComparisonContextEntity;
import vn.ktt.eartraining.infrastructure.persistence.entity.stepcontext.StepContextEntity;

@Component
public class IntervalSoundComparisonContextEntityToDomainMapper implements IStepContextEntityToDomainMapper {

    @Override
    public Class<? extends StepContext> getDataFromClass() {
        return IntervalSoundComparisonContext.class;
    }

    @Override
    public Class<? extends StepContextEntity> getDataToClass() {
        return IntervalSoundComparisonContextEntity.class;
    }

    @Override
    public StepContextEntityMapperKey getKey() {
        return StepContextEntityMapperKey.HIGHER_LOWER_INTERVAL;
    }

    @Override
    public StepContextEntity transform(StepContext dataFrom) {
        var ctx = (IntervalSoundComparisonContext) dataFrom;
        return new IntervalSoundComparisonContextEntity(
                ctx.firstInterval(),
                ctx.secondInterval(),
                ctx.texture(),
                ctx.totalQuestions(),
                ctx.currentQuestionNumber()
        );
    }

    @Override
    public StepContext reverseTransform(StepContextEntity dataTo) {
        var entity = (IntervalSoundComparisonContextEntity) dataTo;
        return new IntervalSoundComparisonContext(
                entity.getFirstInterval(),
                entity.getSecondInterval(),
                entity.getTexture(),
                entity.getTotalQuestions(),
                entity.getCurrentQuestionNumber()
        );
    }
}

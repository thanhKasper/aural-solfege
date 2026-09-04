package vn.ktt.ear_training_system.infrastructure.repository.mapper.step_context;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.IntervalSoundComparisonContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.StepContext;
import vn.ktt.ear_training_system.infrastructure.repository.entities.step_context.IntervalSoundComparisonContextEntity;
import vn.ktt.ear_training_system.infrastructure.repository.entities.step_context.StepContextEntity;

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

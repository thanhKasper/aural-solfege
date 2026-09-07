package vn.ktt.eartraining.application.mapper.stepcontext;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.application.dto.step.IntervalSoundComparisonStepDTO;
import vn.ktt.eartraining.application.dto.step.PracticeStepDTO;
import vn.ktt.eartraining.application.outbound.IIntervalComparisonPort;
import vn.ktt.eartraining.domain.exercise.valueobject.IntervalTexture;
import vn.ktt.eartraining.domain.exercise.valueobject.MusicalInterval;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.IntervalSoundComparisonContext;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.StepContext;

@Component
public class IntervalSoundComparisonStepContextMapper implements IStepContextMapper {

    private final IIntervalComparisonPort intervalComparisonPort;

    public IntervalSoundComparisonStepContextMapper(IIntervalComparisonPort intervalComparisonPort) {
        this.intervalComparisonPort = intervalComparisonPort;
    }

    @Override
    public Class<? extends StepContext> getDataFromClass() {
        return IntervalSoundComparisonContext.class;
    }

    @Override
    public Class<? extends PracticeStepDTO> getDataToClass() {
        return IntervalSoundComparisonStepDTO.class;
    }

    @Override
    public StepContextMapperKey getKey() {
        return StepContextMapperKey.INTERVAL_SOUND_COMPARISON;
    }

    @Override
    public PracticeStepDTO transform(StepContext dataFrom) {
        var context = (IntervalSoundComparisonContext) dataFrom;
        return new IntervalSoundComparisonStepDTO(
                0,
                "",
                context.firstInterval().name(),
                context.secondInterval().name(),
                context.texture().name(),
                this.intervalComparisonPort.compare(context.firstInterval(), context.secondInterval()),
                context.totalQuestions(),
                context.currentQuestionNumber()
        );
    }

    @Override
    public StepContext reverseTransform(PracticeStepDTO dataTo) {
        var d = (IntervalSoundComparisonStepDTO) dataTo;
        return new IntervalSoundComparisonContext(
                MusicalInterval.valueOf(d.getFirstInterval()),
                MusicalInterval.valueOf(d.getSecondInterval()),
                IntervalTexture.valueOf(d.getTexture()),
                d.getTotalQuestions(),
                d.getCurrentQuestionNumber()
        );
    }
}

package vn.ktt.ear_training_system.application.mappers.step_context;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.practice_step.IntervalDistanceComparisonStepDTO;
import vn.ktt.ear_training_system.application.dtos.practice_step.PracticeStepDTO;
import vn.ktt.ear_training_system.application.outbound.IIntervalComparisonPort;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.IntervalSoundComparisonContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.StepContext;

@Component
public class IntervalDistanceComparisonStepContextMapper implements IStepContextMapper {

    private final IIntervalComparisonPort intervalComparisonPort;

    public IntervalDistanceComparisonStepContextMapper(IIntervalComparisonPort intervalComparisonPort) {
        this.intervalComparisonPort = intervalComparisonPort;
    }

    @Override
    public Class<? extends StepContext> getDataFromClass() {
        return IntervalSoundComparisonContext.class;
    }

    @Override
    public Class<? extends PracticeStepDTO> getDataToClass() {
        return IntervalDistanceComparisonStepDTO.class;
    }

    @Override
    public StepContextMapperKey getKey() {
        return StepContextMapperKey.INTERVAL_HIGHER_LOWER;
    }

    @Override
    public PracticeStepDTO transform(StepContext dataFrom) {
        var context = (IntervalSoundComparisonContext) dataFrom;
        return new IntervalDistanceComparisonStepDTO(
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
        var d = (IntervalDistanceComparisonStepDTO) dataTo;
        return new IntervalSoundComparisonContext(
                MusicalInterval.valueOf(d.getFirstInterval()),
                MusicalInterval.valueOf(d.getSecondInterval()),
                IntervalTexture.valueOf(d.getTexture()),
                d.getTotalQuestions(),
                d.getCurrentQuestionNumber()
        );
    }
}

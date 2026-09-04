package vn.ktt.ear_training_system.application.mappers.step_context;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.practice_step.IntervalSoundComparisonStepDTO;
import vn.ktt.ear_training_system.application.dtos.practice_step.PracticeStepDTO;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.IntervalSoundComparisonContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.StepContext;

@Component
public class IntervalSoundComparisonStepContextMapper implements StepContextMapper {

    @Override
    public Class<? extends StepContext> getDataFromClass() {
        return IntervalSoundComparisonContext.class;
    }

    @Override
    public Class<? extends PracticeStepDTO> getDataToClass() {
        return IntervalSoundComparisonStepDTO.class;
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

package vn.ktt.ear_training_system.application.mappers.step_context;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.practice_step.ListenIntervalStepDTO;
import vn.ktt.ear_training_system.application.dtos.practice_step.PracticeStepDTO;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.ListenIntervalContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.StepContext;

@Component
public class ListenIntervalStepContextMapper implements StepContextMapper {

    @Override
    public Class<? extends StepContext> getDataFromClass() {
        return ListenIntervalContext.class;
    }

    @Override
    public Class<? extends PracticeStepDTO> getDataToClass() {
        return ListenIntervalStepDTO.class;
    }

    @Override
    public PracticeStepDTO transform(StepContext dataFrom) {
        var context = (ListenIntervalContext) dataFrom;
        return new ListenIntervalStepDTO(
                0,
                "",
                context.interval().name(),
                context.direction(),
                context.texture().name()
        );
    }

    @Override
    public StepContext reverseTransform(PracticeStepDTO dataTo) {
        var d = (ListenIntervalStepDTO) dataTo;
        return new ListenIntervalContext(
                MusicalInterval.valueOf(d.getInterval()),
                d.getDirection(),
                IntervalTexture.valueOf(d.getTexture())
        );
    }
}

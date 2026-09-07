package vn.ktt.eartraining.application.mapper.stepcontext;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.application.dto.step.ListenIntervalStepDTO;
import vn.ktt.eartraining.application.dto.step.PracticeStepDTO;
import vn.ktt.eartraining.domain.exercise.valueobject.IntervalTexture;
import vn.ktt.eartraining.domain.exercise.valueobject.MusicalInterval;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.ListenIntervalContext;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.StepContext;

@Component
public class ListenIntervalStepContextMapper implements IStepContextMapper {

    @Override
    public Class<? extends StepContext> getDataFromClass() {
        return ListenIntervalContext.class;
    }

    @Override
    public Class<? extends PracticeStepDTO> getDataToClass() {
        return ListenIntervalStepDTO.class;
    }

    @Override
    public StepContextMapperKey getKey() {
        return StepContextMapperKey.SINGLE_INTERVAL;
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

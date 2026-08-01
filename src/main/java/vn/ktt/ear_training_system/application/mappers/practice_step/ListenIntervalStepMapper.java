package vn.ktt.ear_training_system.application.mappers.practice_step;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ListenIntervalStepDTO;
import vn.ktt.ear_training_system.application.dtos.PracticeStepDTO;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeStep;
import vn.ktt.ear_training_system.domain.practice_session.value_object.ListenIntervalContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepType;

@Component
public class ListenIntervalStepMapper implements PracticeStepMapper {

    @Override
    public Class<? extends PracticeStep> getDataFromClass() {
        return PracticeStep.class;
    }

    @Override
    public Class<? extends PracticeStepDTO> getDataToClass() {
        return ListenIntervalStepDTO.class;
    }

    @Override
    public PracticeStepDTO transform(PracticeStep dataFrom) {
        var context = (ListenIntervalContext) dataFrom.getContext();
        return new ListenIntervalStepDTO(
                dataFrom.getActivityPosition(),
                dataFrom.getStatus().name(),
                context.interval().name(),
                context.direction(),
                context.texture().name()
        );
    }

    @Override
    public PracticeStep reverseTransform(PracticeStepDTO dataTo) {
        var d = (ListenIntervalStepDTO) dataTo;
        return new PracticeStep(
                d.getActivityPosition(),
                StepType.LISTEN_INTERVAL,
                vn.ktt.ear_training_system.domain.practice_session.value_object.StepStatus.valueOf(d.getStatus()),
                new ListenIntervalContext(
                        MusicalInterval.valueOf(d.getInterval()),
                        d.getDirection(),
                        IntervalTexture.valueOf(d.getTexture())
                )
        );
    }
}

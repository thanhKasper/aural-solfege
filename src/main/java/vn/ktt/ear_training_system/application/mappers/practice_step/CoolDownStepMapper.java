package vn.ktt.ear_training_system.application.mappers.practice_step;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.practice_step.CoolDownStepDTO;
import vn.ktt.ear_training_system.application.dtos.practice_step.PracticeStepDTO;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeStep;
import vn.ktt.ear_training_system.domain.practice_session.value_object.CoolDownContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepStatus;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepType;

@Component
public class CoolDownStepMapper implements PracticeStepMapper {

    @Override
    public Class<? extends PracticeStep> getDataFromClass() {
        return PracticeStep.class;
    }

    @Override
    public Class<? extends PracticeStepDTO> getDataToClass() {
        return CoolDownStepDTO.class;
    }

    @Override
    public PracticeStepDTO transform(PracticeStep dataFrom) {
        var context = (CoolDownContext) dataFrom.getContext();
        return new CoolDownStepDTO(
                dataFrom.getActivityPosition(),
                dataFrom.getStatus().name(),
                context.restingTimeInSecond()
        );
    }

    @Override
    public PracticeStep reverseTransform(PracticeStepDTO dataTo) {
        var d = (CoolDownStepDTO) dataTo;
        return new PracticeStep(
                d.getActivityPosition(),
                StepType.COOL_DOWN,
                StepStatus.valueOf(d.getStatus()),
                new CoolDownContext(d.getRestingTimeInSecond())
        );
    }
}

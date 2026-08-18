package vn.ktt.ear_training_system.application.mappers.step_context;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.practice_step.CoolDownStepDTO;
import vn.ktt.ear_training_system.application.dtos.practice_step.PracticeStepDTO;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeStep;
import vn.ktt.ear_training_system.domain.practice_session.value_object.CoolDownContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepStatus;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepType;

@Component
public class CoolDownStepContextMapper implements StepContextMapper {

    @Override
    public Class<? extends StepContext> getDataFromClass() {
        return CoolDownContext.class;
    }

    @Override
    public Class<? extends PracticeStepDTO> getDataToClass() {
        return CoolDownStepDTO.class;
    }

    @Override
    public PracticeStepDTO transform(StepContext dataFrom) {
        var context = (CoolDownContext) dataFrom;
        return new CoolDownStepDTO(
                0, // Dummy data
                "", // Dummy data
                context.restingTimeInSecond()
        );
    }

    @Override
    public StepContext reverseTransform(PracticeStepDTO dataTo) {
        var d = (CoolDownStepDTO) dataTo;
        return new CoolDownContext(d.getRestingTimeInSecond());
    }
}

package vn.ktt.ear_training_system.application.mappers.step_context;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.practice_step.CoolDownStepDTO;
import vn.ktt.ear_training_system.application.dtos.practice_step.PracticeStepDTO;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.CoolDownContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.StepContext;

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

package vn.ktt.eartraining.application.mapper.stepcontext;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.application.dto.step.CoolDownStepDTO;
import vn.ktt.eartraining.application.dto.step.PracticeStepDTO;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.CoolDownContext;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.StepContext;

@Component
public class CoolDownStepContextMapper implements IStepContextMapper {

    @Override
    public Class<? extends StepContext> getDataFromClass() {
        return CoolDownContext.class;
    }

    @Override
    public Class<? extends PracticeStepDTO> getDataToClass() {
        return CoolDownStepDTO.class;
    }

    @Override
    public StepContextMapperKey getKey() {
        return StepContextMapperKey.COOL_DOWN;
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

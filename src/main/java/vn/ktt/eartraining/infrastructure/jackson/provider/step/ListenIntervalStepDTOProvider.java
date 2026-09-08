package vn.ktt.eartraining.infrastructure.jackson.provider.step;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.application.dto.step.ListenIntervalStepDTO;
import vn.ktt.eartraining.application.dto.step.PracticeStepDTO;
import vn.ktt.eartraining.application.dto.step.PracticeStepType;

@Component
public class ListenIntervalStepDTOProvider implements IPracticeStepDTOProvider {
    @Override
    public Class<? extends PracticeStepDTO> targetClass() {
        return ListenIntervalStepDTO.class;
    }

    @Override
    public String typeName() {
        return PracticeStepType.LISTEN_INTERVAL.toString();
    }
}

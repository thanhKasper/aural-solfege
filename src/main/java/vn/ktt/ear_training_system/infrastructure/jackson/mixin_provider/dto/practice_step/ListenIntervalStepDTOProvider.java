package vn.ktt.ear_training_system.infrastructure.jackson.mixin_provider.dto.practice_step;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ListenIntervalStepDTO;
import vn.ktt.ear_training_system.application.dtos.PracticeStepDTO;
import vn.ktt.ear_training_system.application.dtos.PracticeStepType;

@Component
public class ListenIntervalStepDTOProvider implements PracticeStepDTOProvider {
    @Override
    public Class<? extends PracticeStepDTO> targetClass() {
        return ListenIntervalStepDTO.class;
    }

    @Override
    public String typeName() {
        return PracticeStepType.LISTEN_INTERVAL.toString();
    }
}

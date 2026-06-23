package vn.ktt.ear_training_system.infrastructure.dto_prefill;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ListenIntervalStepDTO;
import vn.ktt.ear_training_system.application.dtos.PracticeStepDTO;
import vn.ktt.ear_training_system.infrastructure.dto.ApiCallSpec;

import java.util.Map;

@Component
public class ListenIntervalStepApiCallProvider implements StepApiCallProvider {

    @Override
    public Class<? extends PracticeStepDTO> getPracticeStepDTOClass() {
        return ListenIntervalStepDTO.class;
    }

    @Override
    public ApiCallSpec provide(PracticeStepDTO practiceStepDTO) {
        ListenIntervalStepDTO listenIntervalStepDTO = (ListenIntervalStepDTO) practiceStepDTO;
        Map<String, Object> query = Map.of(
                "texture", listenIntervalStepDTO.getTexture(),
                "direction", listenIntervalStepDTO.getDirection(),
                "interval", listenIntervalStepDTO.getInterval());
        return new ApiCallSpec("GET", "http://localhost:8080/api/interval", query, null);
    }
}

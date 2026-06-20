package vn.ktt.ear_training_system.infrastructure.dto_prefill;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import vn.ktt.ear_training_system.application.dtos.ListenIntervalStepDTO;
import vn.ktt.ear_training_system.application.dtos.PracticeStepDTO;

@Component
public class ListenIntervalStepDTOPrefill implements IPracticeStepDTOPrefill {

    @Override
    public Class<? extends PracticeStepDTO> getPracticeStepDTOClass() {
        return ListenIntervalStepDTO.class;
    }

    @Override
    public PracticeStepDTO prefill(PracticeStepDTO practiceStepDTO) {
        ListenIntervalStepDTO listenIntervalStepDTO = (ListenIntervalStepDTO) practiceStepDTO;
        String uri = UriComponentsBuilder.fromUriString("http://localhost:8080/api/interval/")
                        .queryParam("texture", listenIntervalStepDTO.getTexture()).toUriString();
        listenIntervalStepDTO.setApiUrl(uri);
        return listenIntervalStepDTO;
    }
}

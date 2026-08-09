package vn.ktt.ear_training_system.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.ktt.ear_training_system.application.dtos.practice_step.PracticeStepDTO;
import vn.ktt.ear_training_system.application.dtos.SessionResultDTO;
import vn.ktt.ear_training_system.application.dtos.SessionStepDTO;
import vn.ktt.ear_training_system.application.inbound.SessionPort;
import vn.ktt.ear_training_system.infrastructure.dto.ApiCallSpec;
import vn.ktt.ear_training_system.infrastructure.dto.PracticeStepResponse;
import vn.ktt.ear_training_system.infrastructure.dto_prefill.StepApiCallProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "api/sessions")
public class SessionController {
    private final SessionPort sessionPort;
    private final Map<Class<? extends PracticeStepDTO>, StepApiCallProvider> stepApiCallProviderMap = new HashMap<>();

    public SessionController(SessionPort sessionPort,
                             List<StepApiCallProvider> apiCallProviders) {
        this.sessionPort = sessionPort;
        apiCallProviders.forEach((provider) -> {
            this.stepApiCallProviderMap.put(provider.getPracticeStepDTOClass(), provider);
        });
    }

    @PostMapping("/{sessionId}/conclude")
    public ResponseEntity<SessionResultDTO> concludeSession(@PathVariable String sessionId) {
        return ResponseEntity.ok(sessionPort.concludeSession(UUID.fromString(sessionId)));
    }

    @GetMapping("/{sessionId}/result")
    public ResponseEntity<SessionResultDTO> getSessionResult(@PathVariable String sessionId) {
        return ResponseEntity.ok(sessionPort.getSessionResult(UUID.fromString(sessionId)));
    }

    @PostMapping("/{sessionId}/advance")
    public ResponseEntity<PracticeStepResponse> advanceToNextStep(@PathVariable String sessionId) {
        SessionStepDTO response = sessionPort.advanceToNextStep(UUID.fromString(sessionId));
        ApiCallSpec apiCall = getApiCallSpec(response.currentStep());
        return ResponseEntity.ok(new PracticeStepResponse(response.metadata(), response.currentStep(), apiCall));
    }

    private ApiCallSpec getApiCallSpec(PracticeStepDTO step) {
        StepApiCallProvider provider = retrieveProvider(step);
        return provider.provide(step);
    }

    private StepApiCallProvider retrieveProvider(PracticeStepDTO dto) {
        if (stepApiCallProviderMap.containsKey(dto.getClass())) {
            return stepApiCallProviderMap.get(dto.getClass());
        }
        throw new RuntimeException("Not found API call provider for DTO " + dto.getClass());
    }
}

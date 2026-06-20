package vn.ktt.ear_training_system.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.ktt.ear_training_system.application.dtos.PracticeStepDTO;
import vn.ktt.ear_training_system.application.inbound.SessionAdvancePort;
import vn.ktt.ear_training_system.application.inbound.SessionStartPort;
import vn.ktt.ear_training_system.infrastructure.dto_prefill.IPracticeStepDTOPrefill;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "api/sessions")
public class SessionController {
    private final SessionStartPort sessionStartPort;
    private final SessionAdvancePort sessionAdvancePort;
    private final Map<Class<? extends PracticeStepDTO>, IPracticeStepDTOPrefill> dtoPrefillMap = new HashMap<>();

    public SessionController(SessionStartPort sessionStartPort,
                             SessionAdvancePort sessionAdvancePort,
                             List<IPracticeStepDTOPrefill> dtoPrefills) {
        this.sessionStartPort = sessionStartPort;
        this.sessionAdvancePort = sessionAdvancePort;
        dtoPrefills.forEach((dtoPrefill) -> {
            this.dtoPrefillMap.put(dtoPrefill.getPracticeStepDTOClass(), dtoPrefill);
        });
    }

    @PostMapping("/start/{exerciseId}")
    public ResponseEntity<PracticeStepDTO> startSession(@PathVariable String exerciseId) {
        PracticeStepDTO practiceStepDTO = sessionStartPort.startSession(UUID.fromString(exerciseId));
        IPracticeStepDTOPrefill prefillDTO = retrievePrefill(practiceStepDTO);
        practiceStepDTO = prefillDTO.prefill(practiceStepDTO);
        return ResponseEntity.ok(practiceStepDTO);
    }

    @PostMapping("/{sessionId}/advance")
    public ResponseEntity<PracticeStepDTO> advanceToNextStep(@PathVariable String sessionId) {
        PracticeStepDTO practiceStepDTO = sessionAdvancePort.advanceToNextStep(UUID.fromString(sessionId));
        IPracticeStepDTOPrefill prefillDTO = retrievePrefill(practiceStepDTO);
        practiceStepDTO = prefillDTO.prefill(practiceStepDTO);
        return ResponseEntity.ok(practiceStepDTO);
    }

    private IPracticeStepDTOPrefill retrievePrefill(PracticeStepDTO dto) {
        if (dtoPrefillMap.containsKey(dto.getClass())) {
            return dtoPrefillMap.get(dto.getClass());
        }
        throw new RuntimeException("Not found prefill for DTO " + dto.getClass());
    }
}

package vn.ktt.ear_training_system.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.ktt.ear_training_system.application.dtos.PracticeStepDTO;
import vn.ktt.ear_training_system.application.dtos.PracticeStepResponseDTO;
import vn.ktt.ear_training_system.application.inbound.SessionPort;
import vn.ktt.ear_training_system.infrastructure.dto_prefill.IPracticeStepDTOPrefill;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "api/exercises/{exerciseId}/sessions")
public class ExerciseSessionController {
    private final SessionPort sessionPort;
    private final Map<Class<? extends PracticeStepDTO>, IPracticeStepDTOPrefill> dtoPrefillMap = new HashMap<>();

    public ExerciseSessionController(SessionPort sessionPort,
                                     List<IPracticeStepDTOPrefill> dtoPrefills) {
        this.sessionPort = sessionPort;
        dtoPrefills.forEach((dtoPrefill) -> {
            this.dtoPrefillMap.put(dtoPrefill.getPracticeStepDTOClass(), dtoPrefill);
        });
    }

    @PostMapping
    public ResponseEntity<PracticeStepResponseDTO> startSession(@PathVariable String exerciseId) {
        PracticeStepResponseDTO response = sessionPort.startSession(UUID.fromString(exerciseId));
        PracticeStepDTO currentStep = prefillStep(response.currentStep());
        return ResponseEntity.ok(new PracticeStepResponseDTO(response.metadata(), currentStep));
    }

    private PracticeStepDTO prefillStep(PracticeStepDTO step) {
        IPracticeStepDTOPrefill prefillDTO = retrievePrefill(step);
        return prefillDTO.prefill(step);
    }

    private IPracticeStepDTOPrefill retrievePrefill(PracticeStepDTO dto) {
        if (dtoPrefillMap.containsKey(dto.getClass())) {
            return dtoPrefillMap.get(dto.getClass());
        }
        throw new RuntimeException("Not found prefill for DTO " + dto.getClass());
    }
}

package vn.ktt.ear_training_system.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.ktt.ear_training_system.application.dtos.SessionResultDTO;
import vn.ktt.ear_training_system.application.dtos.SessionStepDTO;
import vn.ktt.ear_training_system.application.inbound.SessionPort;
import vn.ktt.ear_training_system.infrastructure.dto.PracticeStepResponse;

import java.util.UUID;

@RestController
@RequestMapping(path = "api/sessions")
public class SessionController {
    private final SessionPort sessionPort;

    public SessionController(SessionPort sessionPort) {
        this.sessionPort = sessionPort;
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
        return ResponseEntity.ok(new PracticeStepResponse(response.metadata(), response.currentStep()));
    }
}

package vn.ktt.eartraining.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.ktt.eartraining.application.dto.SessionResultDTO;
import vn.ktt.eartraining.application.dto.SessionStepDTO;
import vn.ktt.eartraining.application.inbound.ISessionPort;

import java.util.UUID;

@RestController
@PreAuthorize("hasRole('learner')")
@RequestMapping(path = "api/sessions")
public class SessionController {
    private final ISessionPort sessionPort;

    public SessionController(ISessionPort sessionPort) {
        this.sessionPort = sessionPort;
    }

    @PostMapping("/{sessionId}/conclude")
    public ResponseEntity<SessionResultDTO> concludeSession(@PathVariable String sessionId) {
        return ResponseEntity.ok(sessionPort.concludeSession(UUID.fromString(sessionId)));
    }

    @PostMapping("/{sessionId}/advance")
    public ResponseEntity<SessionStepDTO> advanceToNextStep(@PathVariable String sessionId) {
        return ResponseEntity.ok(sessionPort.advanceToNextStep(UUID.fromString(sessionId)));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId) {
        sessionPort.deleteSession(UUID.fromString(sessionId));
        return ResponseEntity.noContent().build();
    }

//    @GetMapping("/{sessionId}")
//    public ResponseEntity<?> getSessionStep(@PathVariable String sessionId, @RequestParam(required = false) Integer step) {
//
//    }
}

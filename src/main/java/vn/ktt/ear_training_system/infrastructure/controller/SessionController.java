package vn.ktt.ear_training_system.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.ktt.ear_training_system.application.dtos.PracticeStepDTO;
import vn.ktt.ear_training_system.application.inbound.SessionStartPort;

import java.util.UUID;

@RestController
@RequestMapping(path = "api/sessions")
public class SessionController {
    private final SessionStartPort sessionStartPort;

    public SessionController(SessionStartPort sessionStartPort) {
        this.sessionStartPort = sessionStartPort;
    }

    @PostMapping
    public ResponseEntity<PracticeStepDTO> startSession(@RequestBody StartSessionRequest request) {
        return ResponseEntity.ok(sessionStartPort.startSession(request.exerciseId()));
    }

    private record StartSessionRequest(UUID exerciseId) {}
}

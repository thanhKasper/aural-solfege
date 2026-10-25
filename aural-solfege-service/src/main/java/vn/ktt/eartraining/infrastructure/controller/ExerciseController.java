package vn.ktt.eartraining.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.ktt.eartraining.application.dto.ExerciseDTO;
import vn.ktt.eartraining.application.dto.SessionDTO;
import vn.ktt.eartraining.application.inbound.IExerciseCreationPort;
import vn.ktt.eartraining.application.inbound.IExerciseRetrievalPort;
import vn.ktt.eartraining.application.inbound.ISessionPort;
import vn.ktt.eartraining.application.services.Page;
import vn.ktt.eartraining.infrastructure.dto.PracticeStepResponse;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize("hasRole('learner')")
@RequestMapping(path = "api/exercises")
public class ExerciseController {
    private final IExerciseCreationPort exerciseCreationService;
    private final IExerciseRetrievalPort exerciseRetrievalService;
    private final ISessionPort sessionPort;

    public ExerciseController(IExerciseCreationPort exerciseCreationService,
                              IExerciseRetrievalPort exerciseRetrievalService,
                              ISessionPort sessionPort) {
        this.exerciseCreationService = exerciseCreationService;
        this.exerciseRetrievalService = exerciseRetrievalService;
        this.sessionPort = sessionPort;
    }

    @GetMapping
    public ResponseEntity<Page<ExerciseDTO>> getExercises(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(exerciseRetrievalService.getExercises(page, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExerciseDTO> getExercises(@PathVariable String id) {
        return ResponseEntity.ok(exerciseRetrievalService.getExerciseById(id));
    }

    @PostMapping
    public ResponseEntity<?> createNewExercise(@RequestBody ExerciseDTO exerciseDTO) {
        exerciseCreationService.createExercise(exerciseDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{exerciseId}/sessions")
    public ResponseEntity<List<SessionDTO>> getSessions(@PathVariable String exerciseId) {
        List<SessionDTO> sessions = sessionPort.getAvailableSessions(UUID.fromString(exerciseId));
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/{exerciseId}/sessions")
    public ResponseEntity<SessionDTO> startSession(@PathVariable String exerciseId) {
        SessionDTO session = sessionPort.startSession(UUID.fromString(exerciseId));
        return ResponseEntity.ok(session);
    }
}

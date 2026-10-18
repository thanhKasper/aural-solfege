package vn.ktt.eartraining.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.ktt.eartraining.application.dto.ExerciseDTO;
import vn.ktt.eartraining.application.dto.SessionStepDTO;
import vn.ktt.eartraining.application.inbound.IExerciseCreationPort;
import vn.ktt.eartraining.application.inbound.IExerciseRetrievalPort;
import vn.ktt.eartraining.application.inbound.ISessionPort;
import vn.ktt.eartraining.application.services.Page;
import vn.ktt.eartraining.infrastructure.dto.PracticeStepResponse;

import java.util.UUID;

@RestController
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

    @PostMapping("/{exerciseId}/sessions")
    public ResponseEntity<PracticeStepResponse> startSession(@PathVariable String exerciseId) {
        SessionStepDTO response = sessionPort.startSession(UUID.fromString(exerciseId));
        return ResponseEntity.ok(new PracticeStepResponse(response.metadata(), response.currentStep()));
    }
}

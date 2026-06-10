package vn.ktt.ear_training_system.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.inbound.ExerciseCreationPort;
import vn.ktt.ear_training_system.application.inbound.ExerciseRetrievalPort;
import vn.ktt.ear_training_system.application.services.Page;

@RestController
@RequestMapping(path = "api/exercises")
public class ExerciseController {
    private final ExerciseCreationPort exerciseCreationService;
    private final ExerciseRetrievalPort exerciseRetrievalService;

    public ExerciseController(ExerciseCreationPort exerciseCreationService, ExerciseRetrievalPort exerciseRetrievalService) {
        this.exerciseCreationService = exerciseCreationService;
        this.exerciseRetrievalService = exerciseRetrievalService;
    }

    @GetMapping
    public ResponseEntity<Page<ExerciseDTO>> getExercises(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(exerciseRetrievalService.getExercises(page, pageSize));
    }

    @PostMapping
    public ResponseEntity<?> createNewExercise(@RequestBody ExerciseDTO exerciseDTO) {
        exerciseCreationService.createExercise(exerciseDTO);
        return ResponseEntity.ok().build();
    }
}

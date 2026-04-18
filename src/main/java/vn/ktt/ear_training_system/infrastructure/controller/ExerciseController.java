package vn.ktt.ear_training_system.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.inbound.ExerciseCreationPort;
import vn.ktt.ear_training_system.application.inbound.ExerciseRetrievalPort;

import java.util.List;

@RestController
@RequestMapping(path = "/exercises")
public class ExerciseController {
    private final ExerciseCreationPort exerciseCreationService;
    private final ExerciseRetrievalPort exerciseRetrievalService;

    public ExerciseController(ExerciseCreationPort exerciseCreationService, ExerciseRetrievalPort exerciseRetrievalService) {
        this.exerciseCreationService = exerciseCreationService;
        this.exerciseRetrievalService = exerciseRetrievalService;
    }

    @GetMapping
    public ResponseEntity<List<ExerciseDTO>> hello() {
        return ResponseEntity.ok(exerciseRetrievalService.getAllExercises());
    }

    @PostMapping
    public ResponseEntity<?> createNewExercise(@RequestBody ExerciseDTO exerciseDTO) {
        System.out.println("Receive exerciseDTO: " + exerciseDTO.toString());
        exerciseCreationService.createExercise(exerciseDTO);
        return ResponseEntity.ok().build();
    }
}

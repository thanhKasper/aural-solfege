package vn.ktt.ear_training_system.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.inbound.ExerciseCreationPort;

@RestController
@RequestMapping(path = "/exercises")
public class ExerciseController {
    private final ExerciseCreationPort exerciseService;
    public ExerciseController(ExerciseCreationPort exerciseService) {
        this.exerciseService = exerciseService;
    }

    @GetMapping
    public String hello() {
        return "Hello to Aural Solfege";
    }

    @PostMapping
    public ResponseEntity<?> createNewExercise(@RequestBody ExerciseDTO exerciseDTO) {
        System.out.println("Receive exerciseDTO: " + exerciseDTO.toString());
        exerciseService.createExercise(exerciseDTO);
        return ResponseEntity.ok().build();
    }
}

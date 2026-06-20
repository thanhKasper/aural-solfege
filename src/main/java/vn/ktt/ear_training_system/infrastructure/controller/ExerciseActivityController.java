package vn.ktt.ear_training_system.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.ktt.ear_training_system.application.dtos.ExerciseActivityType;

@RestController
@RequestMapping("/api/exercise-activities")
public class ExerciseActivityController {
    @GetMapping("{activityType}")
    public ResponseEntity<?> getGuideline(@PathVariable ExerciseActivityType activityType) {
        return ResponseEntity.ok().build();
    }
}

package vn.ktt.musical_components_core.musical_infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("ApiSoundController")
@RequestMapping("/api/intervals")
public class ExerciseAudioController {
    @GetMapping("/{interval}")
    public ResponseEntity<?> getAllInterval(@PathVariable String interval, @RequestParam String texture, @RequestParam String direction) {
        return ResponseEntity.ok().build();
    }
}

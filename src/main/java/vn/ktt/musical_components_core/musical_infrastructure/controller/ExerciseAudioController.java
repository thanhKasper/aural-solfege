package vn.ktt.musical_components_core.musical_infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("ApiSoundController")
@RequestMapping("/api/interval")
public class ExerciseAudioController {
    @GetMapping("/{interval}")
    public ResponseEntity<?> getAllInterval(@PathVariable String interval, @RequestParam String texture) {
        return ResponseEntity.ok().build();
    }
}

package vn.ktt.musical_components_core.musical_infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("IntervalsController")
@RequestMapping("/api/intervals")
public class IntervalsController {
    @GetMapping("/{interval}/random")
    public ResponseEntity<?> getRandomInterval(@PathVariable String interval, @RequestParam String texture) {
        return ResponseEntity.ok("You want to get " + interval + " with " + texture);
    }
}

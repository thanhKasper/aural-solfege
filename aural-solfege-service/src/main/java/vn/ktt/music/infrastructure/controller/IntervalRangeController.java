package vn.ktt.music.infrastructure.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.ktt.music.application.sound.dto.AudioContent;
import vn.ktt.music.application.sound.inbound.IIntervalGeneratorPort;
import vn.ktt.music.domain.composition.Interval;
import vn.ktt.music.domain.factory.IMusicalEntityFactory;

@RestController
@RequestMapping("/api/interval-range")
public class IntervalRangeController {

    private final IIntervalGeneratorPort intervalGeneratorPort;
    private final IMusicalEntityFactory musicalEntityFactory;

    public IntervalRangeController(IIntervalGeneratorPort intervalGeneratorPort,
                                   IMusicalEntityFactory musicalEntityFactory) {
        this.intervalGeneratorPort = intervalGeneratorPort;
        this.musicalEntityFactory = musicalEntityFactory;
    }

    @GetMapping("/{interval}")
    public ResponseEntity<byte[]> getIntervalRange(@PathVariable String interval,
                                                   @RequestParam String texture,
                                                   @RequestParam String direction) {
        var musicalInterval = musicalEntityFactory.getInterval(interval);
        var musicalTexture = Interval.Texture.fromString(texture);

        AudioContent audio = switch (direction.toUpperCase()) {
            case "DOWN" -> intervalGeneratorPort.generateDownwardInterval(musicalInterval, musicalTexture);
            case "UP" -> intervalGeneratorPort.generateUpwardInterval(musicalInterval, musicalTexture);
            default -> throw new IllegalArgumentException("Unknown direction: " + direction); // Need a shared error handling.
        };

        // @TODO: Not totally perfect, need refinement to obey the API standard contentType, header, etc.
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/wav"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + audio.getFileName() + "\"")
                .body(audio.getData());
    }
}

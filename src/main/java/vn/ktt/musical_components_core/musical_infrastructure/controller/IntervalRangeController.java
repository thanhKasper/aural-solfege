package vn.ktt.musical_components_core.musical_infrastructure.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.ktt.musical_components_core.musical_application.sound_controller.dtos.AudioContent;
import vn.ktt.musical_components_core.musical_application.sound_controller.inbound.IntervalGeneratorPort;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Interval;
import vn.ktt.musical_components_core.musical_domains.music_factory.IMusicalEntityFactory;

@RestController("ApiSoundController")
@RequestMapping("/api/interval-range")
public class IntervalRangeController {

    private final IntervalGeneratorPort intervalGeneratorPort;
    private final IMusicalEntityFactory musicalEntityFactory;

    public IntervalRangeController(IntervalGeneratorPort intervalGeneratorPort,
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

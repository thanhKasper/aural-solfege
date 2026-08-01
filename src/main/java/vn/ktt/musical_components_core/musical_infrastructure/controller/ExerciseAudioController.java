package vn.ktt.musical_components_core.musical_infrastructure.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.ktt.musical_components_core.musical_application.sound_controller.dtos.AudioContent;
import vn.ktt.musical_components_core.musical_application.sound_controller.inbound.IntervalSoundGenerator;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Texture;
import vn.ktt.musical_components_core.musical_domains.music_factory.IMusicalEntityFactory;

@RestController("ApiSoundController")
@RequestMapping("/api/interval-range")
public class ExerciseAudioController {

    private final IntervalSoundGenerator intervalSoundGenerator;
    private final IMusicalEntityFactory musicalEntityFactory;

    public ExerciseAudioController(IntervalSoundGenerator intervalSoundGenerator,
                                   IMusicalEntityFactory musicalEntityFactory) {
        this.intervalSoundGenerator = intervalSoundGenerator;
        this.musicalEntityFactory = musicalEntityFactory;
    }

    @GetMapping("/{interval}")
    public ResponseEntity<byte[]> getIntervalRange(@PathVariable String interval,
                                                   @RequestParam String texture,
                                                   @RequestParam String direction) {
        var musicalInterval = musicalEntityFactory.getInterval(interval);
        var musicalTexture = Texture.fromString(texture);

        AudioContent audio = switch (direction.toUpperCase()) {
            case "DOWN" -> intervalSoundGenerator.generateDownwardInterval(musicalInterval, musicalTexture);
            case "UP" -> intervalSoundGenerator.generateUpwardInterval(musicalInterval, musicalTexture);
            default -> throw new IllegalArgumentException("Unknown direction: " + direction);
        };

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + audio.getFileName() + "\"")
                .body(audio.getData());
    }
}

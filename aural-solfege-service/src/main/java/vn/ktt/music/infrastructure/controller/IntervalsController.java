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
@RequestMapping("/api/intervals")
public class IntervalsController {
    private final IIntervalGeneratorPort intervalGenerator;
    private final IMusicalEntityFactory musicalEntityFactory;

    public IntervalsController(IIntervalGeneratorPort intervalGenerator, IMusicalEntityFactory musicalEntityFactory) {
        this.intervalGenerator = intervalGenerator;
        this.musicalEntityFactory = musicalEntityFactory;
    }

    @GetMapping("/{interval}/random")
    public ResponseEntity<?> getRandomInterval(@PathVariable(name="interval") String intervalNotation, @RequestParam String texture) {
        Interval interval = musicalEntityFactory.getInterval(intervalNotation);
        Interval.Texture musicalTexture = Interval.Texture.fromString(texture);
        AudioContent intervalAudioContent = intervalGenerator.generateInterval(interval, musicalTexture);

        // @TODO: Not totally perfect, need refinement to obey the API standard contentType, header, etc.
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/wav"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + intervalAudioContent.getFileName() + "\"")
                .body(intervalAudioContent.getData());
    }
}

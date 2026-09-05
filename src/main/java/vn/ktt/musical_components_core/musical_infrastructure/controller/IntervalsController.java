package vn.ktt.musical_components_core.musical_infrastructure.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.ktt.musical_components_core.musical_application.sound_controller.dtos.AudioContent;
import vn.ktt.musical_components_core.musical_application.sound_controller.inbound.IntervalGeneratorPort;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Interval;
import vn.ktt.musical_components_core.musical_domains.music_factory.IMusicalEntityFactory;

@RestController("IntervalsController")
@RequestMapping("/api/intervals")
public class IntervalsController {
    private final IntervalGeneratorPort intervalGenerator;
    private final IMusicalEntityFactory musicalEntityFactory;

    public IntervalsController(IntervalGeneratorPort intervalGenerator, IMusicalEntityFactory musicalEntityFactory) {
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

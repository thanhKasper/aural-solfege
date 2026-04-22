package vn.ktt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import vn.ktt.ear_training_system.domain.ExerciseBuilder;
import vn.ktt.musical_components_core.musical_domains.music_factory.IMusicalEntityFactory;
import vn.ktt.musical_components_core.musical_domains.music_factory.MusicalEntityFactory;
import vn.ktt.musical_components_core.musical_domains.music_services.IMusicalOperation;
import vn.ktt.musical_components_core.musical_domains.music_services.MusicalOperation;
import vn.ktt.musical_components_core.musical_infrastructure.internal_controller.SoundController;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        var context = SpringApplication.run(Main.class);
        var musicController = context.getBean(SoundController.class, "SoundController");
        musicController.playInterval("P5", "DESCENDING");
    }

    @Bean
    protected ExerciseBuilder exerciseBuilder() {
        return new ExerciseBuilder();
    }

    @Bean
    protected IMusicalEntityFactory musicalEntityFactory() {
        return new MusicalEntityFactory();
    }

    @Bean
    protected IMusicalOperation musicalOperation() {
        return new MusicalOperation();
    }
}

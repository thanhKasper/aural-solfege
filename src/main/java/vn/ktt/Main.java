package vn.ktt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import vn.ktt.ear_training_system.domain.ExerciseFormat;
import vn.ktt.ear_training_system.domain.factory.ExerciseBuilder;
import vn.ktt.ear_training_system.infrastructure.repository.converter.ExerciseFormatMixin;
import vn.ktt.musical_components_core.musical_domains.music_factory.IMusicalEntityFactory;
import vn.ktt.musical_components_core.musical_domains.music_factory.MusicalEntityFactory;
import vn.ktt.musical_components_core.musical_domains.music_services.IMusicalOperation;
import vn.ktt.musical_components_core.musical_domains.music_services.MusicalOperation;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class);
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

    @Bean("domainObjectMapper")
    public ObjectMapper domainObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(ExerciseFormat.class, ExerciseFormatMixin.class);
        return mapper;
    }
}

package vn.ktt.ear_training_system.infrastructure.jackson.configuration;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.ktt.ear_training_system.application.dtos.ExerciseFormatDTO;
import vn.ktt.ear_training_system.infrastructure.jackson.mixin.ExerciseFormatDTOMixin;
import vn.ktt.ear_training_system.infrastructure.jackson.mixinProvider.ExerciseFormatDTOProvider;

import java.util.List;

@Configuration
public class JacksonConfiguration {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer exerciseFormatDtoMixinCustomizer() {
        return builder -> builder.mixIn(ExerciseFormatDTO.class, ExerciseFormatDTOMixin.class);
    }

    @Bean
    public Module exerciseFormatDtoModule(List<ExerciseFormatDTOProvider> providers) {
        var module = new SimpleModule("ExerciseFormatDTOModule");
        for (var provider : providers) {
            module.registerSubtypes(provider.toNamedType());
        }
        return module;
    }
}

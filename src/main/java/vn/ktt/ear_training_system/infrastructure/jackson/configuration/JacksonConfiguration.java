package vn.ktt.ear_training_system.infrastructure.jackson.configuration;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.ktt.ear_training_system.application.dtos.ExerciseActivityDTO;
import vn.ktt.ear_training_system.infrastructure.jackson.mixin.ExerciseActivityDTOMixin;
import vn.ktt.ear_training_system.infrastructure.jackson.mixinProvider.ExerciseActivityDTOProvider;

import java.util.List;

@Configuration
public class JacksonConfiguration {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer exerciseActivityDtoMixinCustomizer() {
        return builder -> builder.mixIn(ExerciseActivityDTO.class, ExerciseActivityDTOMixin.class);
    }

    @Bean
    public Module exerciseActivityDtoModule(List<ExerciseActivityDTOProvider> providers) {
        var module = new SimpleModule("ExerciseActivityDTOModule");
        for (var provider : providers) {
            module.registerSubtypes(provider.toNamedType());
        }
        return module;
    }
}

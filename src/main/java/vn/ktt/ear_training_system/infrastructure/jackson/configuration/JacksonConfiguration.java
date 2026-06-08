package vn.ktt.ear_training_system.infrastructure.jackson.configuration;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.ktt.ear_training_system.application.dtos.ExerciseFormatDTO;
import vn.ktt.ear_training_system.infrastructure.jackson.mixin.ExerciseFormatDTOMixin;
import vn.ktt.ear_training_system.infrastructure.jackson.mixinProvider.ExerciseFormatDTOMixinProvider;

import java.util.List;

@Configuration
public class JacksonConfiguration {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer exerciseFormatDtoMixinCustomizer(List<ExerciseFormatDTOMixinProvider> providers) {
        return builder -> {
            builder.mixIn(ExerciseFormatDTO.class, ExerciseFormatDTOMixin.class);
            for (var provider : providers) {
                builder.mixIn(provider.targetClass(), provider.mixInClass());
            }
        };
    }
}

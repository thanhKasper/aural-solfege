package vn.ktt.eartraining.infrastructure.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.ktt.eartraining.application.dto.activity.ExerciseActivityDTO;
import vn.ktt.eartraining.application.dto.step.PracticeStepDTO;
import vn.ktt.eartraining.infrastructure.jackson.mixin.activity.ExerciseActivityDTOMixin;
import vn.ktt.eartraining.infrastructure.jackson.mixin.step.PracticeStepDTOMixin;
import vn.ktt.eartraining.infrastructure.jackson.provider.activity.IExerciseActivityDTOProvider;
import vn.ktt.eartraining.infrastructure.jackson.provider.step.IPracticeStepDTOProvider;

import java.util.List;

@Configuration
public class JacksonConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer exerciseActivityDtoMixinCustomizer() {
        return builder -> builder.mixIn(ExerciseActivityDTO.class, ExerciseActivityDTOMixin.class);
    }

    @Bean
    public Module exerciseActivityDtoModule(List<IExerciseActivityDTOProvider> providers) {
        var module = new SimpleModule("ExerciseActivityDTOModule");
        for (var provider : providers) {
            module.registerSubtypes(provider.toNamedType());
        }
        return module;
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer practiceStepDtoMixinCustomizer() {
        return builder -> builder.mixIn(PracticeStepDTO.class, PracticeStepDTOMixin.class);
    }

    @Bean
    public Module practiceStepDtoModule(List<IPracticeStepDTOProvider> providers) {
        var module = new SimpleModule("PracticeStepDTOModule");
        for (var provider : providers) {
            module.registerSubtypes(provider.toNamedType());
        }
        return module;
    }
}
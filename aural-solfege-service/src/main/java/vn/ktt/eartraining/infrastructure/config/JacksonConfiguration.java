package vn.ktt.eartraining.infrastructure.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.module.SimpleModule;
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
    public JsonMapperBuilderCustomizer exerciseActivityDtoMixinCustomizer() {
        return builder -> builder.addMixIn(ExerciseActivityDTO.class, ExerciseActivityDTOMixin.class);
    }

    @Bean
    public JacksonModule exerciseActivityDtoModule(List<IExerciseActivityDTOProvider> providers) {
        var module = new SimpleModule("ExerciseActivityDTOModule");
        for (var provider : providers) {
            module.registerSubtypes(provider.toNamedType());
        }
        return module;
    }

    @Bean
    public JsonMapperBuilderCustomizer practiceStepDtoMixinCustomizer() {
        return builder -> builder.addMixIn(PracticeStepDTO.class, PracticeStepDTOMixin.class);
    }

    @Bean
    public JacksonModule practiceStepDtoModule(List<IPracticeStepDTOProvider> providers) {
        var module = new SimpleModule("PracticeStepDTOModule");
        for (var provider : providers) {
            module.registerSubtypes(provider.toNamedType());
        }
        return module;
    }
}

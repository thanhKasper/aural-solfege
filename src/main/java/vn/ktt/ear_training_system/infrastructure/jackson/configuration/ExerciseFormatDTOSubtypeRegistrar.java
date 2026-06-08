package vn.ktt.ear_training_system.infrastructure.jackson.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.infrastructure.jackson.mixinProvider.ExerciseFormatDTOMixinProvider;

import java.util.List;

@Component
public class ExerciseFormatDTOSubtypeRegistrar {
    public ExerciseFormatDTOSubtypeRegistrar(
            ObjectMapper objectMapper,
            List<ExerciseFormatDTOMixinProvider> providers) {
        for (var provider : providers) {
            objectMapper.registerSubtypes(
                    new NamedType(provider.targetClass(), provider.typeName()));
        }
    }
}

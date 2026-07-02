package vn.ktt.ear_training_system.infrastructure.repository.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepContext;
import vn.ktt.ear_training_system.infrastructure.jackson.mixin.entity.step_context.StepContextMixin;
import vn.ktt.ear_training_system.infrastructure.jackson.mixin_provider.entity.step_context.StepContextMixinProvider;

import java.util.List;

@Converter
@Component
public class StepContextConverter implements AttributeConverter<StepContext, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    public StepContextConverter(List<StepContextMixinProvider> mixinProviders) {
        mapper.addMixIn(StepContext.class, StepContextMixin.class);
        for (StepContextMixinProvider provider : mixinProviders) {
            mapper.addMixIn(provider.targetClass(), provider.mixInClass());
            mapper.registerSubtypes(new NamedType(provider.targetClass(), provider.typeName()));
        }
    }

    @Override
    public String convertToDatabaseColumn(StepContext context) {
        if (context == null) return null;
        try {
            return mapper.writeValueAsString(context);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize step context", e);
        }
    }

    @Override
    public StepContext convertToEntityAttribute(String json) {
        if (json == null) return null;
        try {
            return mapper.readValue(json, StepContext.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize step context", e);
        }
    }
}

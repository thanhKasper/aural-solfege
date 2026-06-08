package vn.ktt.ear_training_system.infrastructure.repository.converter;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.ExerciseFormat;
import vn.ktt.ear_training_system.infrastructure.jackson.mixin.ExerciseFormatMixin;
import vn.ktt.ear_training_system.infrastructure.jackson.mixinProvider.ExerciseFormatMixInProvider;
import java.util.List;

@Converter
@Component
public class ExerciseFormatsConverter implements AttributeConverter<List<ExerciseFormat>, String> {
    private final ObjectMapper mapper;

    public ExerciseFormatsConverter(List<ExerciseFormatMixInProvider> mixInProviders) {
        this.mapper = buildMapper(mixInProviders);
    }

    private static ObjectMapper buildMapper(List<ExerciseFormatMixInProvider> mixInProviders) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(ExerciseFormat.class, ExerciseFormatMixin.class);
        for (var provider : mixInProviders) {
            mapper.addMixIn(provider.targetClass(), provider.mixInClass());
            mapper.registerSubtypes(new NamedType(provider.targetClass(), provider.typeName()));
        }
        return mapper;
    }

    @Override
    public String convertToDatabaseColumn(List<ExerciseFormat> formats) {
        if (formats == null) return null;
        try {
            JavaType type = mapper.getTypeFactory()
                    .constructCollectionType(List.class, ExerciseFormat.class);

            String json = mapper.writerFor(type).writeValueAsString(formats);
            return json;
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize exercise formats", e);
        }
    }

    @Override
    public List<ExerciseFormat> convertToEntityAttribute(String json) {
        if (json == null) return null;
        try {
            JavaType type = mapper.getTypeFactory()
                    .constructCollectionType(List.class, ExerciseFormat.class);

            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize exercise formats", e);
        }
    }
}

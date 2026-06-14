package vn.ktt.ear_training_system.infrastructure.repository.converter;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.ExerciseActivity;
import vn.ktt.ear_training_system.infrastructure.jackson.mixin.ExerciseActivityMixin;
import vn.ktt.ear_training_system.infrastructure.jackson.mixinProvider.ExerciseActivityMixInProvider;
import java.util.List;

@Converter
@Component
public class ExerciseActivitiesConverter implements AttributeConverter<List<ExerciseActivity>, String> {
    private final ObjectMapper mapper;

    public ExerciseActivitiesConverter(List<ExerciseActivityMixInProvider> mixInProviders) {
        this.mapper = buildMapper(mixInProviders);
    }

    private static ObjectMapper buildMapper(List<ExerciseActivityMixInProvider> mixInProviders) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(ExerciseActivity.class, ExerciseActivityMixin.class);
        for (var provider : mixInProviders) {
            mapper.addMixIn(provider.targetClass(), provider.mixInClass());
            mapper.registerSubtypes(new NamedType(provider.targetClass(), provider.typeName()));
        }
        return mapper;
    }

    @Override
    public String convertToDatabaseColumn(List<ExerciseActivity> formats) {
        if (formats == null) return null;
        try {
            JavaType type = mapper.getTypeFactory()
                    .constructCollectionType(List.class, ExerciseActivity.class);

            String json = mapper.writerFor(type).writeValueAsString(formats);
            return json;
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize exercise formats", e);
        }
    }

    @Override
    public List<ExerciseActivity> convertToEntityAttribute(String json) {
        if (json == null) return null;
        try {
            JavaType type = mapper.getTypeFactory()
                    .constructCollectionType(List.class, ExerciseActivity.class);

            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize exercise formats", e);
        }
    }
}

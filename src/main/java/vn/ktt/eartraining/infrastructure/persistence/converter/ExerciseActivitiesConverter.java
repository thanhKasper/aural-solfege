package vn.ktt.eartraining.infrastructure.persistence.converter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;
import vn.ktt.eartraining.infrastructure.persistence.entity.activity.ExerciseActivityEntity;

import java.util.List;

@Converter
@Component
public class ExerciseActivitiesConverter implements AttributeConverter<List<ExerciseActivityEntity>, String> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<ExerciseActivityEntity> entities) {
        if (entities == null) return null;
        try {
            JavaType type = mapper.getTypeFactory()
                    .constructCollectionType(List.class, ExerciseActivityEntity.class);
            return mapper.writerFor(type).writeValueAsString(entities);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize exercise activities", e);
        }
    }

    @Override
    public List<ExerciseActivityEntity> convertToEntityAttribute(String json) {
        if (json == null) return null;
        try {
            JavaType type = mapper.getTypeFactory()
                    .constructCollectionType(List.class, ExerciseActivityEntity.class);
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize exercise activities", e);
        }
    }
}

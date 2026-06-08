package vn.ktt.ear_training_system.infrastructure.repository.converter;

import com.fasterxml.jackson.databind.*;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.ExerciseFormat;
import vn.ktt.ear_training_system.domain.interval_training.SingleIntervalExerciseFormat;
import java.util.List;


@Converter
@Component
public class ExerciseFormatsConverter implements AttributeConverter<List<ExerciseFormat>, String> {
    // Build the mapper statically inside the converter itself
    private static final ObjectMapper mapper = buildMapper();

    private static ObjectMapper buildMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(ExerciseFormat.class, ExerciseFormatMixin.class);
        mapper.addMixIn(SingleIntervalExerciseFormat.class, SingleIntervalExerciseFormatMixin.class);
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

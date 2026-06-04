package vn.ktt.ear_training_system.infrastructure.repository.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.ktt.ear_training_system.domain.ExerciseFormat;
import vn.ktt.ear_training_system.infrastructure.repository.mapper.ExerciseFormatMixin;

import java.util.List;

@Converter
public class ExerciseFormatsConverter implements AttributeConverter<List<ExerciseFormat>, String> {

    private static final ObjectMapper mapper = new ObjectMapper()
            .addMixIn(ExerciseFormat.class, ExerciseFormatMixin.class);

    @Override
    public String convertToDatabaseColumn(List<ExerciseFormat> formats) {
        if (formats == null) {
            return null;
        }
        try {
            return mapper.writeValueAsString(formats);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize exercise formats", e);
        }
    }

    @Override
    public List<ExerciseFormat> convertToEntityAttribute(String json) {
        if (json == null) {
            return null;
        }
        try {
            return mapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize exercise formats", e);
        }
    }
}

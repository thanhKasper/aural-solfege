package vn.ktt.ear_training_system.infrastructure.repository.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.infrastructure.repository.entities.step_context.StepContextEntity;

@Converter
@Component
public class StepContextEntityConverter implements AttributeConverter<StepContextEntity, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(StepContextEntity entity) {
        if (entity == null) return null;
        try {
            return mapper.writeValueAsString(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize step context entity", e);
        }
    }

    @Override
    public StepContextEntity convertToEntityAttribute(String json) {
        if (json == null) return null;
        try {
            return mapper.readValue(json, StepContextEntity.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize step context entity", e);
        }
    }
}

package vn.ktt.eartraining.infrastructure.persistence.converter;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;
import vn.ktt.eartraining.infrastructure.persistence.entity.stepcontext.StepContextEntity;

@Converter
@Component
public class StepContextEntityConverter implements AttributeConverter<StepContextEntity, String> {

    private static final ObjectMapper mapper = JsonMapper.builder().build();

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

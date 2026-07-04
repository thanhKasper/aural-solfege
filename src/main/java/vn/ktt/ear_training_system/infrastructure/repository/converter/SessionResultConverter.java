package vn.ktt.ear_training_system.infrastructure.repository.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.infrastructure.repository.entities.SessionResultEntity;

@Converter
@Component
public class SessionResultConverter implements AttributeConverter<SessionResultEntity, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(SessionResultEntity result) {
        if (result == null) return null;
        try {
            return mapper.writeValueAsString(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize session result", e);
        }
    }

    @Override
    public SessionResultEntity convertToEntityAttribute(String json) {
        if (json == null) return null;
        try {
            return mapper.readValue(json, SessionResultEntity.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize session result", e);
        }
    }
}

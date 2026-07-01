package vn.ktt.ear_training_system.infrastructure.repository.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.practice_session.value_object.SessionResult;

@Converter
@Component
public class SessionResultConverter implements AttributeConverter<SessionResult, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(SessionResult result) {
        if (result == null) return null;
        try {
            return mapper.writeValueAsString(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize session result", e);
        }
    }

    @Override
    public SessionResult convertToEntityAttribute(String json) {
        if (json == null) return null;
        try {
            return mapper.readValue(json, SessionResult.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize session result", e);
        }
    }
}

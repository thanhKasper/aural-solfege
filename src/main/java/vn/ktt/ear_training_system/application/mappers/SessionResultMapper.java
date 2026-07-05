package vn.ktt.ear_training_system.application.mappers;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.SessionResultDTO;
import vn.ktt.ear_training_system.domain.practice_session.value_object.SessionResult;

@Component
public class SessionResultMapper {

    public SessionResultDTO toDto(SessionResult domain) {
        if (domain == null) return null;
        return new SessionResultDTO(
                domain.totalSteps(),
                domain.completedSteps(),
                domain.duration().getSeconds()
        );
    }
}

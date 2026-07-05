package vn.ktt.ear_training_system.application.mappers;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.SessionStepDTO;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeSession;

@Component
public class SessionMapper {

    private final StepMapper stepMapper;

    public SessionMapper(StepMapper stepMapper) {
        this.stepMapper = stepMapper;
    }

    public SessionStepDTO toDto(PracticeSession session) {
        return new SessionStepDTO(
                new SessionStepDTO.Metadata(
                        session.getSessionId(),
                        session.getSteps().size(),
                        session.getCurrentStepIndex(),
                        session.isNextStepAvailable()
                ),
                stepMapper.toDto(session.getCurrentStep())
        );
    }
}

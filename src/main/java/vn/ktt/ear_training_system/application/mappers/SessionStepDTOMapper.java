package vn.ktt.ear_training_system.application.mappers;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.SessionStepDTO;
import vn.ktt.ear_training_system.application.mappers.StepMapper;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeSession;

@Component
public class SessionStepDTOMapper {

    private final StepMapper stepMapper;

    public SessionStepDTOMapper(StepMapper stepMapper) {
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

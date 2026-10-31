package vn.ktt.eartraining.application.mapper;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.application.dto.SessionDTO;
import vn.ktt.eartraining.application.dto.SessionStepDTO;
import vn.ktt.eartraining.domain.session.entity.PracticeSession;

@Component
public class SessionMapper {

    private final StepMapper stepMapper;

    public SessionMapper(StepMapper stepMapper) {
        this.stepMapper = stepMapper;
    }

    public SessionStepDTO mapToSessionStepDTO(PracticeSession session) {
        var currentStep = session.getCurrentStep();
        return new SessionStepDTO(
                new SessionStepDTO.Metadata(
                        session.getSessionId(),
                        session.viewTotalSteps(),
                        session.viewCurrentStepIndex(),
                        session.getRepeat(),
                        session.isNextStepAvailable()
                ),
                stepMapper.toDTO(currentStep, session.groupPositionInRepetition(currentStep))
        );
    }

    public SessionDTO mapToSessionDTO(PracticeSession session) {
        return new SessionDTO(
                session.getSessionId().toString(),
                session.viewTotalSteps(),
                session.viewCurrentStepIndex(),
                session.getCreatedAt()
        );
    }
}

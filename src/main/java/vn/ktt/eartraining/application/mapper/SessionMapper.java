package vn.ktt.eartraining.application.mapper;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.application.dto.SessionStepDTO;
import vn.ktt.eartraining.domain.session.entity.PracticeSession;

@Component
public class SessionMapper {

    private final StepMapper stepMapper;

    public SessionMapper(StepMapper stepMapper) {
        this.stepMapper = stepMapper;
    }

    public SessionStepDTO toDto(PracticeSession session, int repetition) {
        return new SessionStepDTO(
                new SessionStepDTO.Metadata(
                        session.getSessionId(),
                        session.getSteps().size(),
                        session.getCurrentStepIndex(),
                        repetition,
                        session.getSteps().size() / repetition,
                        session.isNextStepAvailable()
                ),
                stepMapper.toDTO(session.getCurrentStep())
        );
    }
}

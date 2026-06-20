package vn.ktt.ear_training_system.application.format_mappers;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ListenIntervalStepDTO;
import vn.ktt.ear_training_system.application.dtos.PracticeStepDTO;
import vn.ktt.ear_training_system.application.dtos.PracticeStepType;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeStep;
import vn.ktt.ear_training_system.domain.practice_session.value_object.ListenIntervalContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepType;

@Component
public class ListenIntervalStepMapper implements PracticeStepMapper {

    @Override
    public Class<? extends PracticeStep> getDomainClass() {
        return PracticeStep.class;
    }

    @Override
    public Class<? extends PracticeStepDTO> getDtoClass() {
        return ListenIntervalStepDTO.class;
    }

    @Override
    public PracticeStepDTO toDto(PracticeStep domain) {
        var context = (ListenIntervalContext) domain.getContext();
        return new ListenIntervalStepDTO(
                domain.getStepNumber(),
                domain.getActivityPosition(),
                PracticeStepType.LISTEN_INTERVAL,
                domain.getStatus().name(),
                context.interval().name(),
                context.direction(),
                context.texture().name()
        );
    }

    @Override
    public PracticeStep toDomain(PracticeStepDTO dto) {
        var d = (ListenIntervalStepDTO) dto;
        return new PracticeStep(
                d.getStepNumber(),
                d.getActivityPosition(),
                StepType.LISTEN_INTERVAL,
                vn.ktt.ear_training_system.domain.practice_session.value_object.StepStatus.valueOf(d.getStatus()),
                new ListenIntervalContext(
                        MusicalInterval.valueOf(d.getInterval()),
                        d.getDirection(),
                        IntervalTexture.valueOf(d.getTexture())
                )
        );
    }
}

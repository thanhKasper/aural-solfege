package vn.ktt.ear_training_system.application.mappers.step_context;

import vn.ktt.ear_training_system.application.dtos.practice_step.PracticeStepDTO;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeStep;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepContext;
import vn.ktt.shared.IDataMapper;

public interface StepContextMapper extends IDataMapper<StepContext, PracticeStepDTO> {
}

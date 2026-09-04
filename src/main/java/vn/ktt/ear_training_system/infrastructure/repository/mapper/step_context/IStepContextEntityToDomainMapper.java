package vn.ktt.ear_training_system.infrastructure.repository.mapper.step_context;

import vn.ktt.ear_training_system.domain.practice_session.value_object.step_context.StepContext;
import vn.ktt.ear_training_system.infrastructure.repository.entities.step_context.StepContextEntity;
import vn.ktt.shared.IDataMapper;

public interface IStepContextEntityToDomainMapper extends IDataMapper<StepContext, StepContextEntity> {
}

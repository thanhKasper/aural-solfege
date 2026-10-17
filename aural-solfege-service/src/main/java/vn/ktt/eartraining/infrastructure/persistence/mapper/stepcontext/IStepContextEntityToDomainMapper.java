package vn.ktt.eartraining.infrastructure.persistence.mapper.stepcontext;

import vn.ktt.eartraining.domain.session.valueobject.stepcontext.StepContext;
import vn.ktt.eartraining.infrastructure.persistence.entity.stepcontext.StepContextEntity;
import vn.ktt.shared.IDataMapper;

public interface IStepContextEntityToDomainMapper extends IDataMapper<StepContextEntityMapperKey, StepContext, StepContextEntity> {
}
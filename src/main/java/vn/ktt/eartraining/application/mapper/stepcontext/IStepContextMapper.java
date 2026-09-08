package vn.ktt.eartraining.application.mapper.stepcontext;

import vn.ktt.eartraining.application.dto.step.PracticeStepDTO;
import vn.ktt.eartraining.domain.session.valueobject.stepcontext.StepContext;
import vn.ktt.shared.IDataMapper;

public interface IStepContextMapper extends IDataMapper<StepContextMapperKey, StepContext, PracticeStepDTO> {
}

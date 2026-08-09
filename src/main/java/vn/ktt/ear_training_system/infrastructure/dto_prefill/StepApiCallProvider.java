package vn.ktt.ear_training_system.infrastructure.dto_prefill;

import vn.ktt.ear_training_system.application.dtos.practice_step.PracticeStepDTO;
import vn.ktt.ear_training_system.infrastructure.dto.ApiCallSpec;

public interface StepApiCallProvider {
    Class<? extends PracticeStepDTO> getPracticeStepDTOClass();
    ApiCallSpec provide(PracticeStepDTO practiceStepDTO);
}

package vn.ktt.ear_training_system.infrastructure.dto_prefill;

import vn.ktt.ear_training_system.application.dtos.PracticeStepDTO;

public interface IPracticeStepDTOPrefill {
    Class<? extends PracticeStepDTO> getPracticeStepDTOClass();
    PracticeStepDTO prefill(PracticeStepDTO practiceStepDTO);
}

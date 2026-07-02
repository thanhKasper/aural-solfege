package vn.ktt.ear_training_system.infrastructure.jackson.mixin.dto.practice_step;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class PracticeStepDTOMixin {
}

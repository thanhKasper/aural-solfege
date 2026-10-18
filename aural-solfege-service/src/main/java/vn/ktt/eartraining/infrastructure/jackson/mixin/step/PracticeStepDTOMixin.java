package vn.ktt.eartraining.infrastructure.jackson.mixin.step;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class PracticeStepDTOMixin {
}

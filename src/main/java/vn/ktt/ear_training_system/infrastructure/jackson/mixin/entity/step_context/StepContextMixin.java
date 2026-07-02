package vn.ktt.ear_training_system.infrastructure.jackson.mixin.entity.step_context;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class StepContextMixin {
}

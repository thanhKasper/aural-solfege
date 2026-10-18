package vn.ktt.eartraining.infrastructure.jackson.mixin.activity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class ExerciseActivityDTOMixin {
}

package vn.ktt.ear_training_system.infrastructure.jackson.mixinProvider;

import vn.ktt.ear_training_system.domain.practice_session.value_object.StepContext;

public interface StepContextMixinProvider {
    Class<? extends StepContext> targetClass();
    Class<?> mixInClass();
    String typeName();
}

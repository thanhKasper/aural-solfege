package vn.ktt.ear_training_system.infrastructure.jackson.mixin_provider.entity.step_context;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.domain.practice_session.value_object.ListenIntervalContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepContext;
import vn.ktt.ear_training_system.infrastructure.jackson.mixin.entity.step_context.ListenIntervalContextMixin;

@Component
public class ListenIntervalMixinProvider implements StepContextMixinProvider {

    @Override
    public Class<? extends StepContext> targetClass() {
        return ListenIntervalContext.class;
    }

    @Override
    public Class<?> mixInClass() {
        return ListenIntervalContextMixin.class;
    }

    @Override
    public String typeName() {
        return "LISTEN_INTERVAL";
    }
}

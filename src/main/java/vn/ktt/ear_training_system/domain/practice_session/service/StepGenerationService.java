package vn.ktt.ear_training_system.domain.practice_session.service;

import vn.ktt.ear_training_system.domain.exercise.entity.ExerciseActivity;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepDefinition;
import vn.ktt.shared.IServiceIndex;
import vn.ktt.shared.ServiceRegistry;

import java.util.List;

public class StepGenerationService extends ServiceRegistry<ExerciseActivity, StepGeneration> {

    public StepGenerationService(List<IServiceIndex<ExerciseActivity, StepGeneration>> generatorList) {
        super(generatorList);
    }

    public List<StepDefinition> generate(List<ExerciseActivity> activities) {
        return activities.stream()
                .flatMap(activity -> {
                    var generator = this.getService(activity);
                    if (generator == null) {
                        throw new IllegalArgumentException(
                                "No StepGeneration registered for " + activity.getClass().getSimpleName());
                    }
                    return generator.generate(activity).stream();
                })
                .toList();
    }
}

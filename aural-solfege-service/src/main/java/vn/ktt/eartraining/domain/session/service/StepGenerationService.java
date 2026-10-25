package vn.ktt.eartraining.domain.session.service;

import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.session.valueobject.StepGroupBlueprint;
import vn.ktt.shared.IServiceIndex;
import vn.ktt.shared.ServiceRegistry;

import java.util.List;

public class StepGenerationService extends ServiceRegistry<ExerciseActivity, IStepGeneration> {

    public StepGenerationService(List<IServiceIndex<ExerciseActivity, IStepGeneration>> generatorList) {
        super(generatorList);
    }

    /** Generates one pass over the activities: one group of steps per activity, in order. */
    public List<StepGroupBlueprint> generate(List<ExerciseActivity> activities) {
        return activities.stream()
                .map(activity -> {
                    var generator = this.getService(activity);
                    if (generator == null) {
                        throw new IllegalArgumentException(
                                "No IStepGeneration registered for " + activity.getClass().getSimpleName());
                    }
                    return generator.generate(activity);
                })
                .toList();
    }
}

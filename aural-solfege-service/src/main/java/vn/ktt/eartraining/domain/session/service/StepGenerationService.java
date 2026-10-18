package vn.ktt.eartraining.domain.session.service;

import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.session.valueobject.StepDefinition;
import vn.ktt.shared.IServiceIndex;
import vn.ktt.shared.ServiceRegistry;

import java.util.Collections;
import java.util.List;

public class StepGenerationService extends ServiceRegistry<ExerciseActivity, IStepGeneration> {

    public StepGenerationService(List<IServiceIndex<ExerciseActivity, IStepGeneration>> generatorList) {
        super(generatorList);
    }

    public List<StepDefinition> generate(List<ExerciseActivity> activities, int repetition) {
        var onePass = activities.stream()
                .flatMap(activity -> {
                    var generator = this.getService(activity);
                    if (generator == null) {
                        throw new IllegalArgumentException(
                                "No IStepGeneration registered for " + activity.getClass().getSimpleName());
                    }
                    return generator.generate(activity).stream();
                })
                .toList();

        return Collections.nCopies(repetition, onePass).stream()
                .flatMap(List::stream)
                .toList();
    }
}

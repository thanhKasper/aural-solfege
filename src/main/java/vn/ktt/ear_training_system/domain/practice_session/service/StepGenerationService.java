package vn.ktt.ear_training_system.domain.practice_session.service;

import vn.ktt.ear_training_system.domain.exercise.entity.ExerciseActivity;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepDefinition;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StepGenerationService {
    private final Map<Class<? extends ExerciseActivity>, StepGeneration> generators;

    public StepGenerationService(List<StepGeneration> generatorList) {
        this.generators = generatorList.stream()
                .collect(Collectors.toMap(StepGeneration::activityType, Function.identity()));
    }

    public List<StepDefinition> generate(List<ExerciseActivity> activities) {
        return activities.stream()
                .flatMap(a -> {
                    var generator = generators.get(a.getClass());
                    if (generator == null) {
                        throw new IllegalArgumentException(
                                "No StepGeneration registered for " + a.getClass().getSimpleName());
                    }
                    return generator.generate(a).stream();
                })
                .toList();
    }
}

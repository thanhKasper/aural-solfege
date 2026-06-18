package vn.ktt.ear_training_system.infrastructure.repository.mapper;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ExerciseActivityDTO;
import vn.ktt.ear_training_system.application.dtos.SingleIntervalExerciseActivityDTO;
import vn.ktt.ear_training_system.domain.exercise.entity.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.entity.interval_training.SingleIntervalExerciseActivity;

@Component
public class SingleIntervalExerciseActivityEntityMapper implements IExerciseActivityEntityMapper {

    @Override
    public Class<? extends ExerciseActivity> getExerciseActivityEntityClass() {
        return SingleIntervalExerciseActivity.class;
    }

    @Override
    public ExerciseActivityDTO toExerciseActivityDTO(ExerciseActivity domain) {
        SingleIntervalExerciseActivity singleIntervalExerciseActivity = (SingleIntervalExerciseActivity) domain;
        return new SingleIntervalExerciseActivityDTO(
                singleIntervalExerciseActivity.getInterval().name(),
                singleIntervalExerciseActivity.getSoundProperty().name(),
                singleIntervalExerciseActivity.getPosition()
        );
    }
}

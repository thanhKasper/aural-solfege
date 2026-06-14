package vn.ktt.ear_training_system.infrastructure.repository.mapper;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ExerciseFormatDTO;
import vn.ktt.ear_training_system.application.dtos.SingleIntervalExerciseFormatDTO;
import vn.ktt.ear_training_system.domain.ExerciseActivity;
import vn.ktt.ear_training_system.domain.interval_training.SingleIntervalExerciseActivity;

@Component
public class SingleIntervalExerciseFormatEntityMapper implements IExerciseFormatEntityMapper {

    @Override
    public Class<? extends ExerciseActivity> getExerciseFormatEntityClass() {
        return SingleIntervalExerciseActivity.class;
    }

    @Override
    public ExerciseFormatDTO toExerciseFormatDTO(ExerciseActivity domain) {
        SingleIntervalExerciseActivity singleIntervalExerciseFormat = (SingleIntervalExerciseActivity) domain;
        return new SingleIntervalExerciseFormatDTO(
                singleIntervalExerciseFormat.getInterval().name(),
                singleIntervalExerciseFormat.getSoundProperty().name(),
                singleIntervalExerciseFormat.getPosition()
        );
    }
}

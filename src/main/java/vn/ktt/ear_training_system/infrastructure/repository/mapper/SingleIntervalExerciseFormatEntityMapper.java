package vn.ktt.ear_training_system.infrastructure.repository.mapper;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ExerciseFormatDTO;
import vn.ktt.ear_training_system.application.dtos.SingleIntervalExerciseFormatDTO;
import vn.ktt.ear_training_system.domain.ExerciseFormat;
import vn.ktt.ear_training_system.domain.interval_training.SingleIntervalExerciseFormat;

@Component
public class SingleIntervalExerciseFormatEntityMapper implements IExerciseFormatEntityMapper {

    @Override
    public Class<? extends ExerciseFormat> getExerciseFormatEntityClass() {
        return SingleIntervalExerciseFormat.class;
    }

    @Override
    public ExerciseFormatDTO toExerciseFormatDTO(ExerciseFormat domain) {
        SingleIntervalExerciseFormat singleIntervalExerciseFormat = (SingleIntervalExerciseFormat) domain;
        return new SingleIntervalExerciseFormatDTO(
                singleIntervalExerciseFormat.getInterval().name(),
                singleIntervalExerciseFormat.getSoundProperty().name(),
                singleIntervalExerciseFormat.getPosition()
        );
    }
}

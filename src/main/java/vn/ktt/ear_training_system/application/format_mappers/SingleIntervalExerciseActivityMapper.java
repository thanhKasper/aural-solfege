package vn.ktt.ear_training_system.application.format_mappers;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ExerciseActivityDTO;
import vn.ktt.ear_training_system.application.dtos.SingleIntervalExerciseActivityDTO;
import vn.ktt.ear_training_system.domain.ExerciseActivity;
import vn.ktt.ear_training_system.domain.interval_training.IntervalTexture;
import vn.ktt.ear_training_system.domain.interval_training.MusicalInterval;
import vn.ktt.ear_training_system.domain.interval_training.SingleIntervalExerciseActivity;

@Component
public class SingleIntervalExerciseActivityMapper implements ExerciseActivityMapper {

    @Override
    public Class<? extends ExerciseActivity> getDomainClass() {
        return SingleIntervalExerciseActivity.class;
    }

    @Override
    public Class<? extends ExerciseActivityDTO> getDtoClass() {
        return SingleIntervalExerciseActivityDTO.class;
    }

    @Override
    public ExerciseActivityDTO toDto(ExerciseActivity domain) {
        var f = (SingleIntervalExerciseActivity) domain;
        return new SingleIntervalExerciseActivityDTO(
                f.getInterval().toString(),
                f.getSoundProperty().toString(),
                f.getPosition()
        );
    }

    @Override
    public ExerciseActivity toDomain(ExerciseActivityDTO dto) {
        var d = (SingleIntervalExerciseActivityDTO) dto;
        return new SingleIntervalExerciseActivity(
                IntervalTexture.valueOf(d.getTexture()),
                MusicalInterval.valueOf(d.getInterval()),
                d.position()
        );
    }
}

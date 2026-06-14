package vn.ktt.ear_training_system.application.format_mappers;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ExerciseFormatDTO;
import vn.ktt.ear_training_system.application.dtos.SingleIntervalExerciseFormatDTO;
import vn.ktt.ear_training_system.domain.ExerciseActivity;
import vn.ktt.ear_training_system.domain.interval_training.IntervalTexture;
import vn.ktt.ear_training_system.domain.interval_training.MusicalInterval;
import vn.ktt.ear_training_system.domain.interval_training.SingleIntervalExerciseActivity;

@Component
public class SingleIntervalExerciseFormatMapper implements ExerciseFormatMapper {

    @Override
    public Class<? extends ExerciseActivity> getDomainClass() {
        return SingleIntervalExerciseActivity.class;
    }

    @Override
    public Class<? extends ExerciseFormatDTO> getDtoClass() {
        return SingleIntervalExerciseFormatDTO.class;
    }

    @Override
    public ExerciseFormatDTO toDto(ExerciseActivity domain) {
        var f = (SingleIntervalExerciseActivity) domain;
        return new SingleIntervalExerciseFormatDTO(
                f.getInterval().toString(),
                f.getSoundProperty().toString(),
                f.getPosition()
        );
    }

    @Override
    public ExerciseActivity toDomain(ExerciseFormatDTO dto) {
        var d = (SingleIntervalExerciseFormatDTO) dto;
        return new SingleIntervalExerciseActivity(
                IntervalTexture.valueOf(d.getTexture()),
                MusicalInterval.valueOf(d.getInterval()),
                d.position()
        );
    }
}

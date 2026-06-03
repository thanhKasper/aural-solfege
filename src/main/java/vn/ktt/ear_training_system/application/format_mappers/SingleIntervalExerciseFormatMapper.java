package vn.ktt.ear_training_system.application.format_mappers;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.ExerciseFormatMapper;
import vn.ktt.ear_training_system.application.dtos.ExerciseFormatDTO;
import vn.ktt.ear_training_system.application.dtos.SingleIntervalExerciseFormatDTO;
import vn.ktt.ear_training_system.domain.ExerciseFormat;
import vn.ktt.ear_training_system.domain.TrainingMethodology;
import vn.ktt.ear_training_system.domain.interval_training.IntervalTexture;
import vn.ktt.ear_training_system.domain.interval_training.MusicalInterval;
import vn.ktt.ear_training_system.domain.interval_training.SingleIntervalExerciseFormat;

@Component
public class SingleIntervalExerciseFormatMapper implements ExerciseFormatMapper {

    @Override
    public Class<? extends ExerciseFormat> getDomainClass() {
        return SingleIntervalExerciseFormat.class;
    }

    @Override
    public Class<? extends ExerciseFormatDTO> getDtoClass() {
        return SingleIntervalExerciseFormatDTO.class;
    }

    @Override
    public ExerciseFormatDTO toDto(ExerciseFormat domain) {
        var f = (SingleIntervalExerciseFormat) domain;
        return new SingleIntervalExerciseFormatDTO(
                f.getInterval().toString(),
                f.getSoundProperty().toString(),
                f.getPosition()
        );
    }

    @Override
    public ExerciseFormat toDomain(ExerciseFormatDTO dto, TrainingMethodology trainingMethodology) {
        var d = (SingleIntervalExerciseFormatDTO) dto;
        return new SingleIntervalExerciseFormat(
                trainingMethodology,
                IntervalTexture.valueOf(d.getTexture()),
                MusicalInterval.valueOf(d.getInterval()),
                d.position()
        );
    }
}

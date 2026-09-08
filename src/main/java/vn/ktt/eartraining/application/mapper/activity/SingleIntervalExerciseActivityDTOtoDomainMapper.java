package vn.ktt.eartraining.application.mapper.activity;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.application.dto.activity.ExerciseActivityDTO;
import vn.ktt.eartraining.application.dto.activity.SingleIntervalExerciseActivityDTO;
import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.exercise.valueobject.IntervalTexture;
import vn.ktt.eartraining.domain.exercise.valueobject.MusicalInterval;
import vn.ktt.eartraining.domain.exercise.valueobject.activity.SingleIntervalExerciseActivity;

@Component
public class SingleIntervalExerciseActivityDTOtoDomainMapper implements IExerciseActivityDTOToDomainMapper {

    @Override
    public Class<? extends ExerciseActivity> getDataFromClass() {
        return SingleIntervalExerciseActivity.class;
    }

    @Override
    public Class<? extends ExerciseActivityDTO> getDataToClass() {
        return SingleIntervalExerciseActivityDTO.class;
    }

    @Override
    public ExerciseActivityMapperKey getKey() {
        return ExerciseActivityMapperKey.SINGLE_INTERVAL;
    }

    @Override
    public ExerciseActivityDTO transform(ExerciseActivity dataFrom) {
        var f = (SingleIntervalExerciseActivity) dataFrom;
        return new SingleIntervalExerciseActivityDTO(
                f.getIntervals().stream().map(MusicalInterval::name).toList(),
                f.getSoundProperty().toString(),
                f.getPosition()
        );
    }

    @Override
    public ExerciseActivity reverseTransform(ExerciseActivityDTO dataTo) {
        var d = (SingleIntervalExerciseActivityDTO) dataTo;
        return new SingleIntervalExerciseActivity(
                IntervalTexture.valueOf(d.getTexture()),
                d.getIntervals().stream().map(MusicalInterval::valueOf).toList(),
                d.position()
        );
    }
}

package vn.ktt.eartraining.application.mapper.activity;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.application.dto.activity.ExerciseActivityDTO;
import vn.ktt.eartraining.application.dto.activity.IntervalSoundComparisonExerciseActivityDTO;
import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.exercise.valueobject.IntervalTexture;
import vn.ktt.eartraining.domain.exercise.valueobject.MusicalInterval;
import vn.ktt.eartraining.domain.exercise.valueobject.activity.IntervalSoundComparison;

@Component
public class IntervalSoundComparisonExerciseActivityDTOtoDomainMapper implements IExerciseActivityDTOToDomainMapper {

    @Override
    public Class<? extends ExerciseActivity> getDataFromClass() {
        return IntervalSoundComparison.class;
    }

    @Override
    public Class<? extends ExerciseActivityDTO> getDataToClass() {
        return IntervalSoundComparisonExerciseActivityDTO.class;
    }

    @Override
    public ExerciseActivityMapperKey getKey() {
        return ExerciseActivityMapperKey.INTERVAL_SOUND_COMPARISON;
    }

    @Override
    public ExerciseActivityDTO transform(ExerciseActivity dataFrom) {
        var f = (IntervalSoundComparison) dataFrom;
        return new IntervalSoundComparisonExerciseActivityDTO(
                f.getIntervals().stream().map(MusicalInterval::name).toList(),
                f.getTexture().toString(),
                f.getPosition()
        );
    }

    @Override
    public ExerciseActivity reverseTransform(ExerciseActivityDTO dataTo) {
        var d = (IntervalSoundComparisonExerciseActivityDTO) dataTo;
        return IntervalSoundComparison.construct(
                d.position(),
                IntervalTexture.valueOf(d.getTexture()),
                MusicalInterval.valueOf(d.getIntervals().get(0)),
                MusicalInterval.valueOf(d.getIntervals().get(1))
        );
    }
}

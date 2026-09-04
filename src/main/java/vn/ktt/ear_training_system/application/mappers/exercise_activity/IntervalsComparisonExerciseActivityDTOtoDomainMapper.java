package vn.ktt.ear_training_system.application.mappers.exercise_activity;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.ExerciseActivityDTO;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.IntervalsComparisonExerciseActivityDTO;
import vn.ktt.ear_training_system.domain.exercise.value_object.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;
import vn.ktt.ear_training_system.domain.exercise.value_object.exercise_activity.IntervalsComparison;

@Component
public class IntervalsComparisonExerciseActivityDTOtoDomainMapper implements ExerciseActivityDTOtoDomainMapper {

    @Override
    public Class<? extends ExerciseActivity> getDataFromClass() {
        return IntervalsComparison.class;
    }

    @Override
    public Class<? extends ExerciseActivityDTO> getDataToClass() {
        return IntervalsComparisonExerciseActivityDTO.class;
    }

    @Override
    public ExerciseActivityDTO transform(ExerciseActivity dataFrom) {
        var f = (IntervalsComparison) dataFrom;
        return new IntervalsComparisonExerciseActivityDTO(
                f.getIntervals().stream().map(MusicalInterval::name).toList(),
                f.getTexture().toString(),
                f.getPosition()
        );
    }

    @Override
    public ExerciseActivity reverseTransform(ExerciseActivityDTO dataTo) {
        var d = (IntervalsComparisonExerciseActivityDTO) dataTo;
        return IntervalsComparison.construct(
                d.position(),
                IntervalTexture.valueOf(d.getTexture()),
                MusicalInterval.valueOf(d.getIntervals().get(0)),
                MusicalInterval.valueOf(d.getIntervals().get(1))
        );
    }
}

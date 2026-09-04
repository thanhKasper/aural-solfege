package vn.ktt.ear_training_system.application.mappers.exercise_activity;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.ExerciseActivityDTO;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.IntervalSoundComparisonExerciseActivityDTO;
import vn.ktt.ear_training_system.domain.exercise.value_object.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;
import vn.ktt.ear_training_system.domain.exercise.value_object.exercise_activity.IntervalSoundComparison;

@Component
public class IntervalSoundComparisonExerciseActivityDTOtoDomainMapper implements ExerciseActivityDTOtoDomainMapper {

    @Override
    public Class<? extends ExerciseActivity> getDataFromClass() {
        return IntervalSoundComparison.class;
    }

    @Override
    public Class<? extends ExerciseActivityDTO> getDataToClass() {
        return IntervalSoundComparisonExerciseActivityDTO.class;
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

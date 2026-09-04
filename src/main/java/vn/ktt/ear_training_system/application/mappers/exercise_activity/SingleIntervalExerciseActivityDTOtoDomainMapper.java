package vn.ktt.ear_training_system.application.mappers.exercise_activity;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.ExerciseActivityDTO;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.SingleIntervalExerciseActivityDTO;
import vn.ktt.ear_training_system.domain.exercise.value_object.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.value_object.IntervalTexture;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;
import vn.ktt.ear_training_system.domain.exercise.value_object.exercise_activity.SingleIntervalExerciseActivity;

@Component
public class SingleIntervalExerciseActivityDTOtoDomainMapper implements ExerciseActivityDTOtoDomainMapper {

    @Override
    public Class<? extends ExerciseActivity> getDataFromClass() {
        return SingleIntervalExerciseActivity.class;
    }

    @Override
    public Class<? extends ExerciseActivityDTO> getDataToClass() {
        return SingleIntervalExerciseActivityDTO.class;
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

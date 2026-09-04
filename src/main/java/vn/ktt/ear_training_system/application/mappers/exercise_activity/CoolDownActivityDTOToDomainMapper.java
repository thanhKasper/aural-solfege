package vn.ktt.ear_training_system.application.mappers.exercise_activity;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.CoolDownRestActivityDTO;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.ExerciseActivityDTO;
import vn.ktt.ear_training_system.domain.exercise.value_object.ExerciseActivity;
import vn.ktt.ear_training_system.domain.exercise.value_object.exercise_activity.CoolDownRestActivity;

@Component
public class CoolDownActivityDTOToDomainMapper implements ExerciseActivityDTOtoDomainMapper {
    @Override
    public Class<? extends ExerciseActivity> getDataFromClass() {
        return CoolDownRestActivity.class;
    }

    @Override
    public Class<? extends ExerciseActivityDTO> getDataToClass() {
        return CoolDownRestActivityDTO.class;
    }

    @Override
    public ExerciseActivityDTO transform(ExerciseActivity exerciseActivity) {
        CoolDownRestActivity restActivity = (CoolDownRestActivity) exerciseActivity;
        return new CoolDownRestActivityDTO(restActivity.getPosition(), restActivity.getRestAmountInSecond());
    }

    @Override
    public ExerciseActivity reverseTransform(ExerciseActivityDTO exerciseActivityDTO) {
        CoolDownRestActivityDTO restActivityDTO = (CoolDownRestActivityDTO) exerciseActivityDTO;
        return new CoolDownRestActivity(restActivityDTO.getPosition(), restActivityDTO.getRestTime());
    }
}

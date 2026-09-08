package vn.ktt.eartraining.application.mapper.activity;

import org.springframework.stereotype.Component;
import vn.ktt.eartraining.application.dto.activity.CoolDownRestActivityDTO;
import vn.ktt.eartraining.application.dto.activity.ExerciseActivityDTO;
import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.exercise.valueobject.activity.CoolDownRestActivity;

@Component
public class CoolDownActivityDTOToDomainMapper implements IExerciseActivityDTOToDomainMapper {
    @Override
    public Class<? extends ExerciseActivity> getDataFromClass() {
        return CoolDownRestActivity.class;
    }

    @Override
    public Class<? extends ExerciseActivityDTO> getDataToClass() {
        return CoolDownRestActivityDTO.class;
    }

    @Override
    public ExerciseActivityMapperKey getKey() {
        return ExerciseActivityMapperKey.COOL_DOWN;
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

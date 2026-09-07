package vn.ktt.ear_training_system.application.mappers.exercise_activity;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.ExerciseActivityDTO;
import vn.ktt.ear_training_system.domain.exercise.value_object.ExerciseActivity;
import vn.ktt.shared.DataMapperRegistry;

import java.util.List;

@Component
public class ExerciseActivityDTOToDomainMapperFactory extends DataMapperRegistry<ExerciseActivityMapperKey, ExerciseActivity, ExerciseActivityDTO> {

    public ExerciseActivityDTOToDomainMapperFactory(List<IExerciseActivityDTOToDomainMapper> exerciseActivityDTOtoDomainMappers) {
        super(exerciseActivityDTOtoDomainMappers);
    }

    public ExerciseActivityDTO toExerciseActivityDTO(ExerciseActivity domain) {
        return this.transform(domain);
    }

    public ExerciseActivityDTO toExerciseActivityDTO(ExerciseActivity domain, Class<? extends ExerciseActivityDTO> targetType) {
        return this.transform(domain, targetType);
    }

    public ExerciseActivity toExerciseActivityDomain(ExerciseActivityDTO dto) {
        return this.reverseTransform(dto);
    }

    public ExerciseActivity toExerciseActivityDomain(ExerciseActivityDTO dto, Class<? extends ExerciseActivity> sourceType) {
        return this.reverseTransform(dto, sourceType);
    }
}
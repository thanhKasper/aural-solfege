package vn.ktt.ear_training_system.application.mappers.exercise_activity;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.exercise_activities.ExerciseActivityDTO;
import vn.ktt.ear_training_system.domain.exercise.entity.ExerciseActivity;
import vn.ktt.shared.DataMapperRegistry;

import java.util.List;

@Component
public class ExerciseActivityDTOToDomainMapperFactory extends DataMapperRegistry<ExerciseActivity, ExerciseActivityDTO, ExerciseActivityDTOtoDomainMapper> {
    public ExerciseActivityDTOToDomainMapperFactory(List<ExerciseActivityDTOtoDomainMapper> exerciseActivityDTOtoDomainMappers) {
        super(exerciseActivityDTOtoDomainMappers);
    }

    public ExerciseActivityDTO toExerciseActivityDTO(ExerciseActivity domain) {
        var mapper = getMapperBaseOnDataFrom(domain);
        if (mapper == null && domain.getClass().getSuperclass() != null) {
            mapper = dataFromMapper.get(domain.getClass().getSuperclass());
        }
        if (mapper == null) {
            throw new IllegalArgumentException("No mapper for " + domain.getClass());
        }
        return mapper.transform(domain);
    }

    public ExerciseActivity toExerciseActivityDomain(ExerciseActivityDTO dto) {
        var mapper = getMapperBaseOnDataTo(dto);
        if (mapper == null) {
            throw new IllegalArgumentException("No mapper for format: " + dto.getClass().getSimpleName());
        }
        return mapper.reverseTransform(dto);
    }
}

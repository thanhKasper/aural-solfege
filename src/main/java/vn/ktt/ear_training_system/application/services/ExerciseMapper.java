package vn.ktt.ear_training_system.application.services;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.dtos.ExerciseActivityDTO;
import vn.ktt.ear_training_system.application.format_mappers.ExerciseActivityMapper;
import vn.ktt.ear_training_system.domain.exercise.entity.Exercise;
import vn.ktt.ear_training_system.domain.exercise.entity.ExerciseActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExerciseMapper {
    private final Map<Class<?>, ExerciseActivityMapper> domainIndex;
    private final Map<Class<?>, ExerciseActivityMapper> dtoIndex;

    public ExerciseMapper(List<ExerciseActivityMapper> mappers) {
        this.domainIndex = new HashMap<>();
        this.dtoIndex = new HashMap<>();
        for (var mapper : mappers) {
            domainIndex.put(mapper.getDomainClass(), mapper);
            dtoIndex.put(mapper.getDtoClass(), mapper);
        }
    }

    public ExerciseDTO toExerciseDTO(Exercise exercise) {
        return new ExerciseDTO(
                exercise.getExerciseId().toString(),
                exercise.getTitle(),
                exercise.getDescription(),
                exercise.getTrainingMethodology().name(),
                exercise.isLoop() ? null : exercise.getRepetitions(),
                exercise.getExerciseActivities().stream().map(this::toExerciseActivityDTO).toList(),
                exercise.getRest(),
                exercise.isLoop()
        );
    }

    public ExerciseActivityDTO toExerciseActivityDTO(ExerciseActivity domain) {
        return findMapper(domain).toDto(domain);
    }

    public ExerciseActivity toDomain(ExerciseActivityDTO dto) {
        var mapper = dtoIndex.get(dto.getClass());
        if (mapper == null) {
            throw new IllegalArgumentException("No mapper for format: " + dto.getClass().getSimpleName());
        }
        return mapper.toDomain(dto);
    }

    private ExerciseActivityMapper findMapper(ExerciseActivity domain) {
        Class<?> clazz = domain.getClass();
        var mapper = domainIndex.get(clazz);
        if (mapper == null && clazz.getSuperclass() != null) {
            mapper = domainIndex.get(clazz.getSuperclass());
        }
        if (mapper == null) {
            throw new IllegalArgumentException("No mapper for " + domain.getClass());
        }
        return mapper;
    }
}

package vn.ktt.ear_training_system.application;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.dtos.ExerciseFormatDTO;
import vn.ktt.ear_training_system.application.format_mappers.ExerciseFormatMapper;
import vn.ktt.ear_training_system.domain.Exercise;
import vn.ktt.ear_training_system.domain.ExerciseFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExerciseMapper {
    private final Map<Class<?>, ExerciseFormatMapper> domainIndex;
    private final Map<Class<?>, ExerciseFormatMapper> dtoIndex;

    public ExerciseMapper(List<ExerciseFormatMapper> mappers) {
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
                exercise.getExerciseFormats().stream().map(this::toExerciseFormatDTO).toList(),
                exercise.getRest(),
                exercise.isLoop()
        );
    }

    public ExerciseFormatDTO toExerciseFormatDTO(ExerciseFormat domain) {
        return findMapper(domain).toDto(domain);
    }

    public ExerciseFormat toDomain(ExerciseFormatDTO dto) {
        var mapper = dtoIndex.get(dto.getClass());
        if (mapper == null) {
            throw new IllegalArgumentException("No mapper for format: " + dto.getClass().getSimpleName());
        }
        return mapper.toDomain(dto);
    }

    private ExerciseFormatMapper findMapper(ExerciseFormat domain) {
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

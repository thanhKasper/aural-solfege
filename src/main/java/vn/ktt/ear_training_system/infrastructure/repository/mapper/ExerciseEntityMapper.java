package vn.ktt.ear_training_system.infrastructure.repository.mapper;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.dtos.ExerciseFormatDTO;
import vn.ktt.ear_training_system.domain.Exercise;
import vn.ktt.ear_training_system.domain.ExerciseFormat;
import vn.ktt.ear_training_system.infrastructure.repository.entities.ExerciseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExerciseEntityMapper {
    private final Map<Class<?>, IExerciseFormatMapper> exerciseFormatMapperMap = new HashMap<>();

    public ExerciseEntityMapper(List<IExerciseFormatMapper> exerciseFormatMappers) {
        for (IExerciseFormatMapper exerciseFormatMapper : exerciseFormatMappers) {
            exerciseFormatMapperMap.put(
                    exerciseFormatMapper.getExerciseFormatDTOClass(),
                    exerciseFormatMapper);
        }
    }

    public ExerciseEntity toEntity(Exercise domain) {
        var entity = new ExerciseEntity();
        entity.setExerciseId(domain.getExerciseId());
        entity.setTrainingMethodology(domain.getTrainingMethodology());
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setRepetitions(domain.isLoop() ? null : domain.getRepetitions());
        entity.setRest(domain.getRest());
        entity.setExerciseFormats(domain.getExerciseFormats());
        return entity;
    }

    public Exercise toDomain(ExerciseEntity entity) {
        var entityRepetitions = entity.getRepetitions();
        return new Exercise(
                entity.getExerciseId(),
                entity.getTrainingMethodology(),
                entity.getTitle(),
                entity.getDescription(),
                entityRepetitions == null,
                entityRepetitions == null ? 0 : entityRepetitions,
                entity.getRest(),
                entity.getExerciseFormats()
        );
    }

    public ExerciseDTO toDto(ExerciseEntity entity) {
        List<ExerciseFormatDTO> exerciseFormatDTOs = entity
                .getExerciseFormats().stream().map(this::toExerciseFormatDTO).toList();

        return new ExerciseDTO(
                entity.getExerciseId().toString(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getTrainingMethodology().name(),
                entity.getRepetitions(),
                exerciseFormatDTOs,
                entity.getRest(),
                entity.getRepetitions() == null
        );
    }

    private ExerciseFormatDTO toExerciseFormatDTO(ExerciseFormat exerciseFormat) {
        return getMapper(exerciseFormat.getClass()).toExerciseFormatDTO(exerciseFormat);
    }

    private IExerciseFormatMapper getMapper(Class<?> exerciseFormatClass) {
        if (exerciseFormatMapperMap.containsKey(exerciseFormatClass)) {
            return exerciseFormatMapperMap.get(exerciseFormatClass);
        }
        else {
            throw new IllegalArgumentException("No mapper for " + exerciseFormatClass);
        }
    }
}

package vn.ktt.ear_training_system.application.services;

import org.springframework.stereotype.Component;
import vn.ktt.ear_training_system.application.dtos.PracticeStepDTO;
import vn.ktt.ear_training_system.application.mappers.PracticeStepMapper;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeStep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StepMapper {
    private final Map<Class<?>, PracticeStepMapper> domainIndex;
    private final Map<Class<?>, PracticeStepMapper> dtoIndex;

    public StepMapper(List<PracticeStepMapper> mappers) {
        this.domainIndex = new HashMap<>();
        this.dtoIndex = new HashMap<>();
        for (var mapper : mappers) {
            domainIndex.put(mapper.getDomainClass(), mapper);
            dtoIndex.put(mapper.getDtoClass(), mapper);
        }
    }

    public PracticeStepDTO toDto(PracticeStep domain) {
        return findMapper(domain).toDto(domain);
    }

    public PracticeStep toDomain(PracticeStepDTO dto) {
        var mapper = dtoIndex.get(dto.getClass());
        if (mapper == null) {
            throw new IllegalArgumentException("No mapper for step: " + dto.getClass().getSimpleName());
        }
        return mapper.toDomain(dto);
    }

    private PracticeStepMapper findMapper(PracticeStep domain) {
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

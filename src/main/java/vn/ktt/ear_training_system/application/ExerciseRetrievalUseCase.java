package vn.ktt.ear_training_system.application;

import org.springframework.stereotype.Service;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.inbound.ExerciseRetrievalPort;
import vn.ktt.ear_training_system.domain.IExerciseRepository;

import java.util.List;

@Service
public class ExerciseRetrievalUseCase implements ExerciseRetrievalPort {
    private final IExerciseRepository exerciseRepository;
    private final ExerciseMapper mapper;

    public ExerciseRetrievalUseCase(IExerciseRepository exerciseRepository, ExerciseMapper mapper) {
        this.exerciseRepository = exerciseRepository;
        this.mapper = mapper;
    }

    @Override
    public List<ExerciseDTO> getAllExercises() {
        return exerciseRepository.findAll().stream().map(mapper::toExerciseDTO).toList();
    }
}

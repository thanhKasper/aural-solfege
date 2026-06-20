package vn.ktt.ear_training_system.application;

import org.springframework.stereotype.Service;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.inbound.ExerciseRetrievalPort;
import vn.ktt.ear_training_system.application.services.ExerciseMapper;
import vn.ktt.ear_training_system.application.outbound.IExercisePaginationPort;
import vn.ktt.ear_training_system.application.services.Page;
import vn.ktt.ear_training_system.domain.exercise.repository.IExerciseRepository;

@Service
public class ExerciseRetrievalUseCase implements ExerciseRetrievalPort {
    private final IExercisePaginationPort exercisePaginationService;
    private final IExerciseRepository exerciseRepository;
    private final ExerciseMapper mapper;

    public ExerciseRetrievalUseCase(IExercisePaginationPort exercisePaginationService, IExerciseRepository exerciseRepo, ExerciseMapper mapper) {
        this.exercisePaginationService = exercisePaginationService;
        this.exerciseRepository = exerciseRepo;
        this.mapper = mapper;
    }

    @Override
    public Page<ExerciseDTO> getExercises(int page, int pageSize) {
        return exercisePaginationService.getPagedExercises(page, pageSize);
    }

    @Override
    public ExerciseDTO getExerciseById(String id) {
        return mapper.toExerciseDTO(exerciseRepository.getExerciseById(id));
    }
}

package vn.ktt.ear_training_system.application;

import org.springframework.stereotype.Service;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.inbound.ExerciseRetrievalPort;
import vn.ktt.ear_training_system.application.services.IExercisePaginationService;
import vn.ktt.ear_training_system.application.services.Page;
import vn.ktt.ear_training_system.domain.repository.IExerciseRepository;

@Service
public class ExerciseRetrievalUseCase implements ExerciseRetrievalPort {
    private final IExercisePaginationService exercisePaginationService;
    private final IExerciseRepository exerciseRepository;
    private final ExerciseMapper mapper;

    public ExerciseRetrievalUseCase(IExercisePaginationService exercisePaginationService, IExerciseRepository exerciseRepo, ExerciseMapper mapper) {
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

package vn.ktt.eartraining.application;

import org.springframework.stereotype.Service;
import vn.ktt.eartraining.application.dto.ExerciseDTO;
import vn.ktt.eartraining.application.inbound.IExerciseRetrievalPort;
import vn.ktt.eartraining.application.mapper.ExerciseMapper;
import vn.ktt.eartraining.application.outbound.IExercisePaginationPort;
import vn.ktt.eartraining.application.services.Page;
import vn.ktt.eartraining.domain.exercise.repository.IExerciseRepository;

@Service
public class ExerciseRetrievalUseCase implements IExerciseRetrievalPort {
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
        return exercisePaginationService.getPagedExercises(page, pageSize)
                .map(mapper::toExerciseDTO);
    }

    @Override
    public ExerciseDTO getExerciseById(String id) {
        return mapper.toExerciseDTO(exerciseRepository.getExerciseById(id));
    }
}

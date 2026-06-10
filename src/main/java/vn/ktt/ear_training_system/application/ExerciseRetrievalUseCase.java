package vn.ktt.ear_training_system.application;

import org.springframework.stereotype.Service;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.inbound.ExerciseRetrievalPort;
import vn.ktt.ear_training_system.application.services.IExercisePaginationService;
import vn.ktt.ear_training_system.application.services.Page;

@Service
public class ExerciseRetrievalUseCase implements ExerciseRetrievalPort {
    private final IExercisePaginationService exercisePaginationService;

    public ExerciseRetrievalUseCase(IExercisePaginationService exercisePaginationService) {
        this.exercisePaginationService = exercisePaginationService;
    }

    @Override
    public Page<ExerciseDTO> getExercises(int page, int pageSize) {
        return exercisePaginationService.getPagedExercises(page, pageSize);
    }
}

package vn.ktt.eartraining.application.inbound;

import vn.ktt.eartraining.application.dto.ExerciseDTO;
import vn.ktt.eartraining.application.services.Page;

public interface IExerciseRetrievalPort {
    Page<ExerciseDTO> getExercises(int page, int pageSize);
    ExerciseDTO getExerciseById(String id);
}

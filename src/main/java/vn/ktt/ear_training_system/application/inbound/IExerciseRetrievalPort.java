package vn.ktt.ear_training_system.application.inbound;

import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.services.Page;

public interface IExerciseRetrievalPort {
    Page<ExerciseDTO> getExercises(int page, int pageSize);
    ExerciseDTO getExerciseById(String id);
}

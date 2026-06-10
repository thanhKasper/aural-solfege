package vn.ktt.ear_training_system.application.inbound;

import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.services.Page;

public interface ExerciseRetrievalPort {
    Page<ExerciseDTO> getExercises(int page, int pageSize);
}

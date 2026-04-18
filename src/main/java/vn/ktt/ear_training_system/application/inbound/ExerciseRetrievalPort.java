package vn.ktt.ear_training_system.application.inbound;

import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;

import java.util.List;

public interface ExerciseRetrievalPort {
    List<ExerciseDTO> getAllExercises();
}

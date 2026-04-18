package vn.ktt.ear_training_system.application.inbound;

import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;

public interface ExerciseCreationPort {
    void createExercise(ExerciseDTO exerciseDTO);
}

package vn.ktt.ear_training_system.application.inbound;

import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;

public interface IExerciseCreationPort {
    void createExercise(ExerciseDTO exerciseDTO);
}

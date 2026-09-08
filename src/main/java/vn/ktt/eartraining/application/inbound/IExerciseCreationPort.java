package vn.ktt.eartraining.application.inbound;

import vn.ktt.eartraining.application.dto.ExerciseDTO;

public interface IExerciseCreationPort {
    void createExercise(ExerciseDTO exerciseDTO);
}

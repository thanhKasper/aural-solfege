package vn.ktt.ear_training_system.application;

import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.domain.IExerciseRepository;

public class CreateExerciseUseCase {
    private final IExerciseRepository exerciseRepository;
    public CreateExerciseUseCase(IExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }
    public void createExercise(ExerciseDTO exerciseDTO) {

    }
}

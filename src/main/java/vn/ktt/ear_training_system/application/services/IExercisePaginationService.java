package vn.ktt.ear_training_system.application.services;

import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;

public interface IExercisePaginationService {
    Page<ExerciseDTO> getPagedExercises(int page, int pageSize);
}

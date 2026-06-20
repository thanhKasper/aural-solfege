package vn.ktt.ear_training_system.application.outbound;

import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;

public interface IExercisePaginationPort {
    Page<ExerciseDTO> getPagedExercises(int page, int pageSize);
}

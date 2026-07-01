package vn.ktt.ear_training_system.application.outbound;

import vn.ktt.ear_training_system.application.services.Page;
import vn.ktt.ear_training_system.domain.exercise.entity.Exercise;

public interface IExercisePaginationPort {
    Page<Exercise> getPagedExercises(int page, int pageSize);
}

package vn.ktt.eartraining.application.outbound;

import vn.ktt.eartraining.application.services.Page;
import vn.ktt.eartraining.domain.exercise.entity.Exercise;

public interface IExercisePaginationPort {
    Page<Exercise> getPagedExercises(int page, int pageSize);
}

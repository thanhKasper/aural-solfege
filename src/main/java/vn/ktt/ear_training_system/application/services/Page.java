package vn.ktt.ear_training_system.application.services;

import java.util.List;
import java.util.function.Function;

public record Page<T>(int page, int pageSize, int totalPages, int totalElements, boolean hasNext, boolean hasPrevious,
                      List<T> content) {

    public <U> Page<U> map(Function<T, U> mapper) {
        return new Page<>(page, pageSize, totalPages, totalElements, hasNext, hasPrevious,
                content.stream().map(mapper).toList());
    }
}

package vn.ktt.ear_training_system.application.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Page<T> {
    private final int page;
    private final int pageSize;
    private final int totalPages;
    private final int totalElements;
    private final boolean hasNext;
    private final boolean hasPrevious;
    private final List<T> content;
}

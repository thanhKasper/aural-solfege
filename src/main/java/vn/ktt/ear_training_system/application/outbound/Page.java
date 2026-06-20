package vn.ktt.ear_training_system.application.outbound;

import java.util.List;

public record Page<T>(int page, int pageSize, int totalPages, int totalElements, boolean hasNext, boolean hasPrevious,
                      List<T> content) {
}

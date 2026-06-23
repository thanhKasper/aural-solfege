package vn.ktt.ear_training_system.infrastructure.dto;

import java.util.Map;

public record ApiCallSpec(
        String method,
        String url,
        Map<String, Object> query,
        Map<String, Object> body
) {}

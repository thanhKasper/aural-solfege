package vn.ktt.eartraining.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class SessionDTO {
    private String sessionId;
    private int totalSteps;
    private int completedSteps;
    private Instant createdAt;
}

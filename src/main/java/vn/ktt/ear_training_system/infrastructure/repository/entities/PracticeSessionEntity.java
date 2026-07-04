package vn.ktt.ear_training_system.infrastructure.repository.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.ktt.ear_training_system.domain.practice_session.value_object.SessionStatus;
import vn.ktt.ear_training_system.infrastructure.repository.converter.SessionResultConverter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "practice_sessions")
@Getter
@Setter
@NoArgsConstructor
public class PracticeSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID sessionId;

    @Column(name = "exercise_id", nullable = false)
    private UUID exerciseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status;

    @Column(name = "current_step_index", nullable = false)
    private int currentStepIndex;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderColumn(name = "step_order")
    private List<PracticeStepEntity> steps;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Convert(converter = SessionResultConverter.class)
    @Column(name = "result", columnDefinition = "TEXT")
    private SessionResultEntity result;
}

package vn.ktt.ear_training_system.infrastructure.repository.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepContext;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepStatus;
import vn.ktt.ear_training_system.domain.practice_session.value_object.StepType;
import vn.ktt.ear_training_system.infrastructure.repository.converter.StepContextConverter;

import java.util.UUID;

@Entity
@Table(name = "practice_steps")
@Getter
@Setter
@NoArgsConstructor
public class PracticeStepEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PracticeSessionEntity session;

    @Column(name = "step_number", nullable = false)
    private int stepNumber;

    @Column(name = "activity_position", nullable = false)
    private int activityPosition;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false)
    private StepType stepType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StepStatus status;

    @Convert(converter = StepContextConverter.class)
    @Column(name = "context", columnDefinition = "TEXT", nullable = false)
    private StepContext context;
}

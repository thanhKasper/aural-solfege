package vn.ktt.eartraining.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import vn.ktt.eartraining.domain.session.valueobject.StepStatus;
import vn.ktt.eartraining.domain.session.valueobject.StepType;
import vn.ktt.eartraining.infrastructure.persistence.converter.StepContextEntityConverter;
import vn.ktt.eartraining.infrastructure.persistence.entity.stepcontext.StepContextEntity;

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

    @Convert(converter = StepContextEntityConverter.class)
    @Column(name = "context", columnDefinition = "TEXT", nullable = false)
    private StepContextEntity context;
}
